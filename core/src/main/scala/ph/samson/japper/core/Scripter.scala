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

import java.util.jar.{Attributes, JarFile}

import com.typesafe.scalalogging.StrictLogging
import org.eclipse.aether.artifact.Artifact

/**
  * TODO: Document Scripter
  */
object Scripter extends StrictLogging {

  def bashScript(artifact: Artifact, dependencies: Seq[Artifact]) = {
    val jarFile = new JarFile(artifact.getFile)
    for {
      manifest <- Option(jarFile.getManifest)
      mainAttributes = manifest.getMainAttributes
      mainClass <- Option(mainAttributes.getValue(Attributes.Name.MAIN_CLASS))
    } yield {
      logger.debug(s"main class: $mainClass")

      val classPath = (artifact +: dependencies)
        .map(_.getFile)
        .mkString(java.io.File.pathSeparator)

      s"""|#!/bin/sh
          |
          |java -cp $classPath $mainClass "$$@"
          |""".stripMargin

    }
  }

}
