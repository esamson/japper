package ph.samson.japper.core

import better.files.File
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.{RepositorySystem, RepositorySystemSession}
import org.eclipse.aether.repository.{LocalRepository, RemoteRepository}
import org.scalatest.{Matchers, Outcome, fixture}

import scala.util.Try

class ResolverTest extends fixture.FlatSpec with Matchers {

  case class FixtureParam(repoSystem: RepositorySystem,
                          session: RepositorySystemSession,
                          remoteRepo: RemoteRepository)

  override def withFixture(test: OneArgTest): Outcome = {
    val repoSystem = Resolver.newRepositorySystem()
    val session = {
      val repoDir = File.newTemporaryDirectory(
        "repo",
        test.configMap
          .getOptional[String]("tmpdir")
          .map(t => File(t))) / "japper-test-repo"
      val localRepo = new LocalRepository(repoDir.toJava)
      val s = MavenRepositorySystemUtils.newSession()
      s.setLocalRepositoryManager(
        repoSystem.newLocalRepositoryManager(s, localRepo))
      s
    }
    withFixture(
      test.toNoArgTest(
        FixtureParam(repoSystem, session, Resolver.MavenCentral)))
  }

  behavior of "ResolverTest"

  it should "resolve" in { fixtureParam =>
    implicit val FixtureParam(r, s, c) = fixtureParam
    Resolver.resolve("com.google.googlejavaformat", "google-java-format", None)
  }

  it should "not resolve" in { fixtureParam =>
    implicit val FixtureParam(r, s, c) = fixtureParam
    val resolved =
      Try(Resolver.resolve("com.example.bad", "no-such-thing", Some("1")))
    println(s"resolved: $resolved")
  }
}
