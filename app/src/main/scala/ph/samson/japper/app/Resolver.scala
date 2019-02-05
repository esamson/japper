package ph.samson.japper.app

/**
  * TODO: Document Resolver
  */
trait Resolver[F[_]] {
  import Resolver._

  def getLatestVersion(groupId: String, artifactId: String): F[Option[Version]]

  def resolve(groupId: String,
              artifactId: String,
              version: Version): F[Option[Resolution]]
}

object Resolver {

  final case class Version(value: String) extends AnyVal

  final case class File(path: String) extends AnyVal

  final case class Artifact(groupId: String,
                            artifactId: String,
                            version: Version,
                            file: File)

  final case class Resolution(artifact: Artifact, dependencies: List[Artifact])
}
