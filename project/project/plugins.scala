import sbt._

object Plugin extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn(
    uri("git://github.com/xuwei-k/conscript-plugin.git#sbt0.13.0")
  )
}
