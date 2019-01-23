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

package ph.samson.japper.installer

import java.nio.file.attribute.PosixFilePermission

import better.files.File._
import com.typesafe.scalalogging.StrictLogging
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.repository.LocalRepository
import ph.samson.japper.core.Dirs.RepoDir
import ph.samson.japper.core.{Resolver, Scripter}

object Main extends StrictLogging {

  def main(args: Array[String]): Unit = {
    logger.debug(s"main(${args.mkString(", ")})")

    implicit val repoSystem = Resolver.newRepositorySystem()
    implicit val session = newSession(repoSystem)
    implicit val remoteRepo = Resolver.MavenCentral

    val launchScript = for {
      artifacts <- Resolver.resolve("ph.samson.japper", "japper-app_2.12", None)
      mainArtifact = artifacts.head
      dependencies = artifacts.tail
      script <- Scripter.bashScript(mainArtifact, dependencies)
    } yield {
      val name = mainArtifact.getArtifactId.stripSuffix("_2.12")
      val target = currentWorkingDirectory / name
      target.write(script)
      target.addPermission(PosixFilePermission.OWNER_EXECUTE)
      target
    }

    launchScript match {
      case Some(s) => logger.info(s"wrote launch script: $s")
      case None    => logger.warn("Could not create launch script")
    }
  }

  def newSession(system: RepositorySystem) = {
    val session = MavenRepositorySystemUtils.newSession()

    val localRepo = new LocalRepository(RepoDir.toJava)
    session.setLocalRepositoryManager(
      system.newLocalRepositoryManager(session, localRepo))

    session
  }
}
