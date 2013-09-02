# giter8

## giter8-plugin for sbt0.13

`project/project/plugins.scala`

```scala
import sbt._
object PluginDef extends Build {
  lazy val root = Project("plugins", file(".")) dependsOn(
    ProjectRef(uri("git://github.com/xuwei-k/giter8#sbt0.13.0"), "giter8-plugin")
  )
}
```
