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

import com.typesafe.scalalogging.StrictLogging
import fs2.Stream

object Main extends StrictLogging {

  def main(args: Array[String]): Unit = {
    logger.debug(s"main(${args.mkString(", ")})")

    val command = Command(args)
    val program = command match {
      case Install(groupId, artifactId, version) =>
        Installer.install(groupId, artifactId, version)
    }

    val result = Stream
      .eval(program)
      .compile
      .drain
      .redeem(
        err => {
          logger.error(s"$command failed.", err)
          -1
        },
        _ => {
          0
        }
      )
      .unsafeRunSync()

    sys.exit(result)
  }
}
