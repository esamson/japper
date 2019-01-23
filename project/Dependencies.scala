import sbt._

object Dependencies {

  val betterFiles = "com.github.pathikrit" %% "better-files" % "3.7.0"

  val resolverVersion = "1.3.1"
  val mavenResolverConnectorBasic = "org.apache.maven.resolver" % "maven-resolver-connector-basic" % resolverVersion
  val mavenResolverTransportFile = "org.apache.maven.resolver" % "maven-resolver-transport-file" % resolverVersion
  val mavenResolverTransportHttp = "org.apache.maven.resolver" % "maven-resolver-transport-http" % resolverVersion

  val mavenResolverProvider = "org.apache.maven" % "maven-resolver-provider" % "3.6.0"

  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.25"

  val scallop = "org.rogach" %% "scallop" % "3.1.5"

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
}
