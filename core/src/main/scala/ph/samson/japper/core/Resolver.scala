/*
 * Copyright (C) 2019  Edward Samson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ph.samson.japper.core

import com.typesafe.scalalogging.StrictLogging
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.{DependencyRequest, VersionRangeRequest}
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import org.eclipse.aether.{RepositorySystem, RepositorySystemSession}

import scala.collection.JavaConverters._

/**
  * TODO: Document Resolver
  */
object Resolver extends StrictLogging {

  def resolve(groupId: String, artifactId: String, version: Option[String])(
      implicit repoSystem: RepositorySystem,
      session: RepositorySystemSession,
      remoteRepo: RemoteRepository) = {
    logger.debug(s"resolve($groupId, $artifactId, $version)($remoteRepo)")

    version
      .orElse(resolveVersion(groupId, artifactId))
      .map(v => doResolve(groupId, artifactId, v))
  }

  /**
    * Get latest version of the given artifact.
    *
    * @return the resolve version or None if artifact cannot be resolved.
    */
  def resolveVersion(groupId: String, artifactId: String)(
      implicit repoSystem: RepositorySystem,
      session: RepositorySystemSession,
      remoteRepo: RemoteRepository) = {
    val req = new VersionRangeRequest()
      .addRepository(remoteRepo)
      .setArtifact(new DefaultArtifact(groupId, artifactId, "jar", "[,)"))
    Option(repoSystem.resolveVersionRange(session, req).getHighestVersion)
      .map(_.toString)
  }

  private def doResolve(groupId: String, artifactId: String, version: String)(
      implicit repoSystem: RepositorySystem,
      session: RepositorySystemSession,
      remoteRepo: RemoteRepository) = {

    val artifact = new DefaultArtifact(groupId, artifactId, "jar", version)
    val dependency = new Dependency(artifact, "compile")

    logger.debug(s"collecting $dependency")
    val collectRequest = new CollectRequest()
    collectRequest.setRoot(dependency)
    collectRequest.addRepository(remoteRepo)
    val node = repoSystem.collectDependencies(session, collectRequest).getRoot

    logger.debug(s"resolving $node")
    val dependencyRequest = new DependencyRequest()
    dependencyRequest.setRoot(node)
    repoSystem.resolveDependencies(session, dependencyRequest)

    logger.debug("traversing")
    val nlg = new PreorderNodeListGenerator
    node.accept(nlg)

    val resolvedArtifacts = nlg.getArtifacts(false)
    val allArtifacts = nlg.getArtifacts(true)

    if (resolvedArtifacts != allArtifacts) {
      val unresolved = allArtifacts.asScala -- resolvedArtifacts.asScala
      logger.warn(s"Unresolved artifacts:\n  ${unresolved.mkString("\n  ")}")
    }

    resolvedArtifacts.asScala.toList
  }

  def newRepositorySystem() = {
    val locator = MavenRepositorySystemUtils.newServiceLocator()
    locator.addService(classOf[RepositoryConnectorFactory],
                       classOf[BasicRepositoryConnectorFactory])
    locator.addService(classOf[TransporterFactory],
                       classOf[FileTransporterFactory])
    locator.addService(classOf[TransporterFactory],
                       classOf[HttpTransporterFactory])

    locator.getService(classOf[RepositorySystem])
  }

  val MavenCentral: RemoteRepository =
    new RemoteRepository.Builder("central",
                                 "default",
                                 "https://repo1.maven.org/maven2/").build()
}
