import sbt._
import sbt.Keys._
import sbtassembly.AssemblyPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import scalafix.sbt.ScalafixPlugin.autoImport._

/**
  * Base project settings
  */
object Base {

  def japperProject(name: String): Project = {
    val dir = file(name)
    val base = Project(s"japper-$name", dir)
      .disablePlugins(plugins.JUnitXmlReportPlugin)
      .settings(Settings)

    val withAssembly = AssemblySettings(name)
      .map(s => base.settings(s: _*))
      .getOrElse(base.disablePlugins(AssemblyPlugin))

    val withIt = if ((dir / "src" / "it").isDirectory) {
      withAssembly
        .configs(IntegrationTest)
        .settings(
          Defaults.itSettings,
          IntegrationTest / testOptions += Tests.Argument(
            TestFrameworks.ScalaTest,
            "-oG",
            "-u",
            s"${target.value}/test-reports"
          ),
          IntegrationTest / logBuffered := false,
          IntegrationTest / fork := true,
          IntegrationTest / javaOptions += "-Xmx2G",
          IntegrationTest / parallelExecution := true
        )
    } else {
      withAssembly
    }

    withIt
  }

  lazy val Settings = Seq(
    // https://tpolecat.github.io/2017/04/25/scalac-flags.html
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8", // Specify character encoding used by source files.
      "-explaintypes", // Explain type errors in more detail.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds", // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
      "-Xfuture", // Turn on future language features.
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
      "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
      "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
      "-Xlint:option-implicit", // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
      "-Xlint:unsound-match", // Pattern match may not be typesafe.
      "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
      "-Ypartial-unification", // Enable partial unification in type constructor inference
      "-Yrangepos", // Required by SemanticDB compiler plugin
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
      "-Ywarn-numeric-widen", // Warn when numerics are widened.
      "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:params", // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates", // Warn if a private member is unused.
      "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
    ),
    Compile / console / scalacOptions --= Seq(
      "-Ywarn-unused-import",
      "-Xfatal-warnings"
    ),
    scalacOptions ++= (
      // Fail the compilation if there are any warnings.
      if (sys.env.contains("STRICT")) {
        Seq("-Xfatal-warnings")
      } else {
        Nil
      }
    ),
    javacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-Werror",
      "-Xlint:all",
      "-Xlint:-serial"
    ),
    Test / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oG",
      "-u",
      s"${target.value}/test-reports"
    ),
    Test / logBuffered := false,
    Test / fork := true,
    Test / parallelExecution := true,
    headerLicense := Some(GplV3Only),
    addCompilerPlugin(scalafixSemanticdb)
  )

  lazy val GplV3Only = HeaderLicense.Custom(
    s"""|Copyright (C) 2019  Edward Samson
        |
        |This program is free software: you can redistribute it and/or modify
        |it under the terms of the GNU General Public License as published by
        |the Free Software Foundation, version 3.
        |
        |This program is distributed in the hope that it will be useful,
        |but WITHOUT ANY WARRANTY; without even the implied warranty of
        |MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        |GNU General Public License for more details.
        |
        |You should have received a copy of the GNU General Public License
        |along with this program.  If not, see <http://www.gnu.org/licenses/>.
        |""".stripMargin
  )
}
