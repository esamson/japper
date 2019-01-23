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

import java.net.URL
import java.util.jar.Attributes.Name.{
  IMPLEMENTATION_TITLE,
  IMPLEMENTATION_VENDOR,
  IMPLEMENTATION_VERSION
}
import java.util.jar.Manifest

import org.rogach.scallop.{ScallopConf, Subcommand}

import scala.collection.JavaConverters._

sealed abstract class Command

case class Install(
    groupId: String,
    artifactId: String,
    version: Option[String]
) extends Command

object Command {

  def apply(args: Seq[String]) = new Cli(args).conf

  private class Cli(args: Seq[String]) extends ScallopConf(args) {

    printedName = "japper"

    version(s"$printedName $manifestVersion")

    banner(
      s"""|Usage: japper [--version] [--help]
          |              <command> [<args>]
          |""".stripMargin
    )

    val InstallCommand = new Subcommand("install") {
      val groupId = opt[String](
        descr = "group identifier of the artifact",
        required = true,
        argName = "id"
      )
      val artifactId = opt[String](
        descr = "artifact identifier of the artifact",
        required = true,
        argName = "id"
      )
      val version = opt[String](
        descr = "version of the artifact",
        required = false,
        argName = "v"
      )
    }

    addSubcommand(InstallCommand)

    verify()

    def conf = {
      import scala.language.reflectiveCalls

      val command = for (s <- subcommand) yield {
        s match {
          case InstallCommand =>
            Install(InstallCommand.groupId(),
                    InstallCommand.artifactId(),
                    InstallCommand.version.toOption)
        }
      }
      command.getOrElse({
        printHelp()
        sys.exit(0)
      })
    }
  }

  private def manifestVersion: String = {
    val cl = getClass.getClassLoader

    def read(url: URL) = {
      val stream = url.openStream()
      try {
        new Manifest(stream)
      } finally {
        stream.close()
      }
    }

    val versions = for {
      url <- cl.getResources("META-INF/MANIFEST.MF").asScala
      attributes = read(url).getMainAttributes
      if attributes.getValue(IMPLEMENTATION_VENDOR) == "ph.samson.japper" &&
        attributes.getValue(IMPLEMENTATION_TITLE) == "japper-app"
    } yield attributes.getValue(IMPLEMENTATION_VERSION)

    versions.toList.headOption.getOrElse("(dev)")
  }
}
