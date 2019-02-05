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

/**
  * TODO: Document Resolver
  */
trait Resolver[F[_]] {
  import Resolver._

  def getLatestVersion(groupId: String, artifactId: String): F[Option[Version]]

  def resolve(groupId: String,
              artifactId: String,
              version: Version): F[Option[Application]]
}

object Resolver {

  final case class Version(value: String) extends AnyVal

  final case class File(path: String) extends AnyVal

  final case class Artifact(groupId: String,
                            artifactId: String,
                            version: Version,
                            file: File)

  final case class Application(main: Artifact, dependencies: List[Artifact])
}
