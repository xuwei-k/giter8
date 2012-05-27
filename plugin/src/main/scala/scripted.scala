/** copied from https://github.com/harrah/xsbt/blob/0.11/scripted/plugin/ScriptedPlugin.scala.
 * since ScriptedPlugin is not within a package, it cannot be reused from a packaged class.
 */

package giter8

import sbt._

import Project.Initialize
import Keys._
import classpath.ClasspathUtilities
import java.lang.reflect.Method
import java.util.Properties

object Scripted {
  def scriptedConf = config("g8-scripted-sbt") hide

  val scriptedSbt = SettingKey[String]("_g8-scripted-sbt")
  val sbtLauncher = SettingKey[File]("_g8-sbt-launcher")
  val sbtTestDirectory = SettingKey[File]("_g8-sbt-test-directory")
  val scriptedBufferLog = SettingKey[Boolean]("_g8-scripted-buffer-log")
  final case class ScriptedScalas(build: String, versions: String)
  val scriptedScalas = SettingKey[ScriptedScalas]("_g8-scripted-scalas")

  val scriptedClasspath = TaskKey[PathFinder]("_g8-scripted-classpath")
  val scriptedTests = TaskKey[AnyRef]("_g8-scripted-tests")
  val scriptedRun = TaskKey[Method]("_g8-scripted-run")
  val scriptedLaunchOpts = SettingKey[Seq[String]]("_g8_scripted-launch-opts", "options to pass to jvm launching scripted tasks")
  val scriptedDependencies = TaskKey[Unit]("_g8-scripted-dependencies")
  val scripted = InputKey[Unit]("_g8-scripted")

  def scriptedTestsTask: Initialize[Task[AnyRef]] = (scriptedClasspath, scalaInstance) map {
    (classpath, scala) =>
    val loader = ClasspathUtilities.toLoader(classpath, scala.loader)
    ModuleUtilities.getObject("sbt.test.ScriptedTests", loader)
  }

  def scriptedRunTask: Initialize[Task[Method]] = (scriptedTests) map {
    (m) =>
    m.getClass.getMethod("run", classOf[File], classOf[Boolean], classOf[String], classOf[String], classOf[String], classOf[Array[String]], classOf[File], classOf[Array[String]])
  }

  def scriptedTask: Initialize[InputTask[Unit]] = InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
    (scriptedDependencies, scriptedTests, scriptedRun, sbtTestDirectory, scriptedBufferLog, scriptedSbt, scriptedScalas, sbtLauncher, scriptedLaunchOpts, result) map {
      (deps, m, r, testdir, bufferlog, version, scriptedScalas, launcher, launchOpts, args) =>
      try { r.invoke(m, testdir, bufferlog: java.lang.Boolean, version.toString, scriptedScalas.build, scriptedScalas.versions, args.toArray, launcher, launchOpts.toArray) }
      catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
    }
  }

  lazy val defaultLaunchOps = {
    import scala.collection.JavaConverters._
    val args = Seq("-Xmx","-Xms")
    management.ManagementFactory.getRuntimeMXBean().getInputArguments().asScala.filter(a => args.contains(a) || a.startsWith("-XX")).toSeq
  }

  lazy val scriptedSettings: Seq[sbt.Project.Setting[_]] = Seq(
    ivyConfigurations += scriptedConf,
    scriptedSbt <<= (appConfiguration)(_.provider.id.version),
    scriptedScalas <<= (scalaVersion) { (scala) => ScriptedScalas(scala, scala) },
    libraryDependencies <<= (libraryDependencies, scriptedSbt) {(deps, version) => deps :+ "org.scala-sbt" % "scripted-sbt" % version % scriptedConf.toString },
    sbtLauncher <<= (appConfiguration)(app => IO.classLocationFile(app.provider.scalaProvider.launcher.getClass)),
    sbtTestDirectory <<= sourceDirectory / "sbt-test",
    scriptedBufferLog := true,
    scriptedClasspath <<= (classpathTypes, update) map { (ct, report) => PathFinder(Classpaths.managedJars(scriptedConf, ct, report).map(_.data)) },
    scriptedTests <<= scriptedTestsTask,
    scriptedRun <<= scriptedRunTask,
    scriptedDependencies <<= (compile in Test, publishLocal) map { (analysis, pub) => Unit },
    scriptedLaunchOpts := defaultLaunchOps,
    scripted <<= scriptedTask
  )
}
