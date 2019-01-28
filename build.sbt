import Base._
import Dependencies._

// Global settings
ThisBuild / organization := "ph.samson.japper"
ThisBuild / scalaVersion := "2.12.8"

ThisBuild / licenses := Seq(
  "GPL-3.0-only" -> url("https://spdx.org/licenses/GPL-3.0-only.html"))
ThisBuild / homepage := Some(url("https://github.com/esamson/japper"))
ThisBuild / developers := List(
  Developer(
    id = "esamson",
    name = "Edward Samson",
    email = "edward@samson.ph",
    url = url("https://edward.samson.ph")
  )
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/esamson/japper"),
    "scm:git:git@github.com:esamson/japper.git"
  )
)

ThisBuild / dynverSonatypeSnapshots := true

// Root project
name := "japper"
disablePlugins(AssemblyPlugin)
disablePlugins(HeaderPlugin)
publishArtifact := false

lazy val app = japperProject("app")
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      scallop
    ),
    libraryDependencies ++= Seq(
      slf4jSimple
    ).map(_ % Runtime),
  )
  .settings(
    Compile / packageBin / packageOptions +=
      Package.ManifestAttributes("Japper-Name" -> "japper")
  )

lazy val core = japperProject("core").settings(
  libraryDependencies ++= Seq(
    betterFiles,
    mavenResolverConnectorBasic,
    mavenResolverTransportFile,
    mavenResolverTransportHttp,
    mavenResolverProvider,
    scalaLogging
  ),
  libraryDependencies ++= Seq(
    scalaTest,
    slf4jSimple
  ).map(_ % Test),
  Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest,
                                       s"-Dtmpdir=${target.value}")
)

lazy val installer = japperProject("installer")
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      slf4jSimple
    ).map(_ % Runtime),
  )
