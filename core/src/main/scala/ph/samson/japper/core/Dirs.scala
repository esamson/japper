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

import better.files.File
import better.files.File._

object Dirs {

  val XdgDataHome: File = sys.env
    .get("XDG_DATA_HOME")
    .map(d => File(d))
    .getOrElse(home / ".local" / "share")

  val JapperHome: File = XdgDataHome / "japper"
  val BinDir: File = JapperHome / "bin"
  val RepoDir: File = JapperHome / "repo"
}
