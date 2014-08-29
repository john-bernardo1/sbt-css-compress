import com.typesafe.sbt.web.SbtWeb
import SbtWeb.autoImport._
import WebKeys._

organization := "net.ground5hark.sbt"

name := "sbt-css-compress-test"

version := "0.1.3"

scalaVersion := "2.10.4"

lazy val root = (project in file(".")).enablePlugins(SbtWeb)

CssCompress.parentDir := "css-compress"

pipelineStages := Seq(cssCompress)

def verifyMin(topDir: File, log: Logger): Unit = {
  var notMinified = false
  var notMinifiedName = ""
  val minFiles = (topDir / "css" ** "*.min.css").get.takeWhile(f => !notMinified).foreach {
    minFile: File =>
      val minifiedContents = IO.read(minFile)
      val unminifiedFile = minFile.getParentFile /  minFile.getName.replace(".min", "")
      val unminifiedContents = IO.read(unminifiedFile)
      notMinifiedName = minFile.getAbsolutePath
      log.info(s"Comparing ${minFile.getName} to ${unminifiedFile.getName}...")
      notMinified = minifiedContents.size >= unminifiedContents.size
  }
  if (notMinified)
    sys.error(s"File was not minified properly: $notMinifiedName")
}

val verifyMinified = taskKey[Unit]("Verify that the contents of staging assets are minified")

verifyMinified := {
  verifyMin(stagingDirectory.value, streams.value.log)
}

val verifyAssetsMinified = taskKey[Unit]("Verify that the contents of assets are minified")

verifyAssetsMinified := {
  verifyMin((public in Assets).value, streams.value.log)
}
