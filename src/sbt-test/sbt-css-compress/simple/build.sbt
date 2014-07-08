import com.typesafe.sbt.web.SbtWeb
import SbtWeb.autoImport._
import WebKeys._

organization := "net.ground5hark.sbt"

name := "sbt-css-compress-test"

version := "0.1.0"

scalaVersion := "2.10.4"

lazy val root = (project in file(".")).enablePlugins(SbtWeb)

pipelineStages := Seq(cssCompress)

val verifyMinified = taskKey[Unit]("Verify that the contents are minified")

verifyMinified := {
  var notMinified = false
  var notMinifiedName = ""
  val minFiles = (stagingDirectory.value / "css" ** "*.min.css").get.takeWhile(f => !notMinified).foreach {
    minFile: File =>
      val minifiedContents = IO.read(minFile)
      val unminifiedFile = minFile.getParentFile /  minFile.getName.replace(".min", "")
      val unminifiedContents = IO.read(unminifiedFile)
      notMinifiedName = minFile.getAbsolutePath
      streams.value.log.info(s"Comparing ${minFile.getName} to ${unminifiedFile.getName}...")
      notMinified = minifiedContents.size >= unminifiedContents.size
  }
  if (notMinified)
    sys.error(s"File was not minified properly: $notMinifiedName")
}

