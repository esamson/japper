import sbt.Keys._
import sbt._
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin.defaultShellScript
import sbtassembly.{MergeStrategy, PathList}

object AssemblySettings {

  /** Projects that need to produce assembly JARs must be "registered" here by
    * adding an entry. If project needs no extra settings then add with a `Nil`
    * value.
    */
  private val settings: Map[String, Seq[Def.SettingsDefinition]] = Map(
    "installer" -> Seq(
      assembly / assemblyJarName := s"${name.value}-${version.value}.sh",
      assembly / assemblyMergeStrategy := strategies(SisuNamed),
      assembly / assemblyOption := (assemblyOption in assembly).value
        .copy(prependShellScript = Some(defaultShellScript))
    )
  )

  def apply(name: String): Option[Seq[Def.SettingsDefinition]] =
    settings.get(name).map(_ ++ Common)

  lazy val Common = Seq(
    Compile / assembly / artifact := {
      val art = (Compile / assembly / artifact).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(Compile / assembly / artifact, assembly),
    assembly / test := {}
  )

  lazy val DefaultMerge: PartialFunction[String, MergeStrategy] = {
    case any => MergeStrategy.defaultMergeStrategy(any)
  }

  lazy val SisuNamed: PartialFunction[String, MergeStrategy] = {
    case PathList("META-INF", "sisu", "javax.inject.Named") =>
      MergeStrategy.filterDistinctLines
  }

  /** Combine the given partial merge strategies, falling back in the given
    * order and ultimately falling back to the default merge strategy.
    *
    * @param pfs partial merge strategies
    * @return combined merge strategy
    */
  def strategies(pfs: PartialFunction[String, MergeStrategy]*)
    : PartialFunction[String, MergeStrategy] =
    pfs.foldRight(DefaultMerge)((l, r) => l orElse r)
}
