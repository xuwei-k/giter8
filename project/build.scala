import sbt._

object Builds extends sbt.Build {
  import Keys._

  val g8version = "0.4.6-SNAPSHOT"

  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "net.databinder.giter8",
    version := g8version,
    scalaVersion := "2.9.2",
    libraryDependencies ++= Seq(
      "org.clapper" % "scalasti_2.9.1" % "0.5.8"),
    publishArtifact in (Compile, packageBin) := true,
    homepage :=
      Some(url("https://github.com/n8han/giter8")),
    publishMavenStyle := true,
    publishTo :=
      Some("releases" at
           "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
    publishArtifact in Test := false,
    licenses := Seq("LGPL v3" -> url("http://www.gnu.org/licenses/lgpl.txt")),
    pomExtra := (
      <scm>
        <url>git@github.com:n8han/giter8.git</url>
        <connection>scm:git:git@github.com:n8han/giter8.git</connection>
      </scm>
      <developers>
        <developer>
          <id>n8han</id>
          <name>Nathan Hamblen</name>
          <url>http://twitter.com/n8han</url>
        </developer>
      </developers>)
  )

  // posterous title needs to be giter8, so both app and root are named giter8
  lazy val root = Project("root", file("."),
    settings = buildSettings ++ Seq(
      name := "giter8"
    )) aggregate(plugin, app, lib)

  lazy val app = Project("app", file("app"),
    settings = buildSettings ++ Seq(
      description :=
        "Command line tool to apply templates defined on github",
      name := "giter8",
      libraryDependencies +=
        "net.databinder" % "dispatch-lift-json_2.9.1" % "0.8.5"
    )) dependsOn (lib)

  lazy val plugin = Project("giter8-plugin", file("plugin"),
    settings = buildSettings ++ Seq(
      description :=
        "sbt 0.12 plugin for testing giter8 templates",
      sbtPlugin := true,
      resolvers += Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns),
      libraryDependencies <++= (sbtVersion) { (sv) =>
        Seq(
          "org.scala-sbt" % "scripted-plugin" % sv
        )
      }
    )) dependsOn (lib)

  lazy val lib = Project("giter8-lib", file("library"),
    settings = buildSettings ++ Seq(
      description :=
        "shared library for app and plugin",
      libraryDependencies <++= (sbtDependency){(sd) =>
        Seq(sd,
          "me.lessis" % "ls_2.9.1" % "0.1.2-RC2"
        )
      }
    ))
}
