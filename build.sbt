sbtPlugin := true

organization := "net.ground5hark.sbt"

name := "sbt-css-compress"

version := "0.2.0-SNAPSHOT"

scalaVersion := "2.10.6"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("sbt snapshot plugins", url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.mavenLocal
)

def addCrossSbtPlugin(dependency: ModuleID): Setting[Seq[ModuleID]] =
  libraryDependencies += {
    val sbtV = (sbtBinaryVersion in pluginCrossBuild).value
    val scalaV = (scalaBinaryVersion in update).value
    Defaults.sbtPluginExtra(dependency, sbtV, scalaV)
  }

addCrossSbtPlugin("com.typesafe.sbt" %% "sbt-web" % "1.4.3")

libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.8"

publishMavenStyle := true

crossSbtVersions := Seq("0.13.16", "1.0.2")

scriptedSettings
scriptedLaunchOpts ++= Seq(
  "-Xmx1024M",
  s"-Dproject.version=${version.value}"
)
//scriptedBufferLog := false

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  Some(if (isSnapshot.value) {
    "snapshots" at nexus + "content/repositories/snapshots"
  } else {
    "releases" at nexus + "service/local/staging/deploy/maven2"
  })
}

pomExtra := {
  <url>https://github.com/ground5hark/sbt-css-compress</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>http://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:ground5hark/sbt-css-compress.git</url>
      <connection>scm:git:git@github.com:ground5hark/sbt-css-compress.git</connection>
    </scm>
    <developers>
      <developer>
        <id>ground5hark</id>
        <name>John Bernardo</name>
        <url>https://github.com/ground5hark</url>
      </developer>
    </developers>
}
