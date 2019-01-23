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

package ph.samson.japper.app

import java.nio.file.attribute.PosixFilePermission

import better.files.File
import better.files.File._
import com.typesafe.scalalogging.StrictLogging
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.repository.LocalRepository
import ph.samson.japper.core.Dirs._
import ph.samson.japper.core.{Resolver, Scripter}

object Installer extends StrictLogging {

  def install(groupId: String, artifactId: String, version: Option[String]) = {

    implicit val repoSystem = Resolver.newRepositorySystem()
    implicit val session = newSession(repoSystem)
    implicit val remoteRepo = Resolver.MavenCentral

    val launchScript = for {
      artifacts <- Resolver.resolve(groupId, artifactId, version)
      mainArtifact = artifacts.head
      dependencies = artifacts.tail
      script <- Scripter.bashScript(mainArtifact, dependencies)
    } yield {
      val name = mainArtifact.getArtifactId.stripSuffix("_2.12")

      if (BinDir.notExists) {
        BinDir.createDirectories()
      }
      val target = BinDir / name
      target.write(script)
      target.addPermission(PosixFilePermission.OWNER_EXECUTE)
      target
    }

    launchScript match {
      case Some(s) =>
        logger.debug(s"wrote launch script: $s")
        linkScript(s)
      case None => logger.warn("Could not create launch script")
    }

  }

  private def linkScript(script: File): Unit = {
    val targetDir = home / ".local" / "bin"
    if (targetDir.notExists) {
      targetDir.createDirectories()
    }

    val link = targetDir / script.name
    if (link.notExists) {
      link.symbolicLinkTo(script)
      logger.info(s"Installed to $link")
    } else {
      if (link.isSymbolicLink) {
        val real = link.path.toRealPath()
        if (real.isSameFileAs(script)) {
          logger.info(s"Updated $link")
        } else {
          logger.warn(
            s"Cannot update $link because it refers to unmanaged path $real")
        }
      } else {
        logger.warn(s"Cannot update unmanaged path $link")
      }
    }

    if (!sys.env.getOrElse("PATH", "").contains(targetDir.toString())) {
      logger.warn(s"Add $targetDir to your environment PATH.")
    }
  }

  private def newSession(system: RepositorySystem) = {
    val session = MavenRepositorySystemUtils.newSession()

    val localRepo = new LocalRepository(RepoDir.toJava)
    session.setLocalRepositoryManager(
      system.newLocalRepositoryManager(session, localRepo))

    session
  }
}
