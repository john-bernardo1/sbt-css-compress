sbt-css-compress
===========
[![Build Status](https://api.travis-ci.org/ground5hark/sbt-css-compress.png?branch=master)](https://travis-ci.org/ground5hark/sbt-css-compress)

[sbt-web] plugin which compresses CSS using [yuicompressor].

Plugin
======
Add the plugin to your `project/plugins.sbt`:
```scala
addSbtPlugin("net.ground5hark.sbt" % "sbt-css-compress" % "0.1.0")
```

Add the [Sonatype releases] resolver:
```scala
resolvers += Resolver.sonatypeRepo("releases")
```

Enable the [sbt-web] plugin for your project:
```scala
lazy val root = (project in file(".")).enablePlugins(SbtWeb)
```

Add the `css-compress` task to your asset pipeline in your `build.sbt`:
```scala
pipelineStages := Seq(cssCompress)
```

Configuration options
=====================
Option              | Description
--------------------|------------
suffix              | Extension to append to each compressed file. Defaults to `".min.css"`
parentDir           | Parent directory name where compressed CSS will go. Defaults to `"css-compress"`
lineBreak           | Number of characters on a line before attempting to insert a line break. Defaults to `-1` (never)

An example of providing an option is below:

```scala
CssCompress.suffix := ".min.css"
```

This will produce assets with the specified `CssCompress.suffix` suffix value under the `CssCompress.parentDir`
directory within the `target` folder. This will be `target/web/public/main` or `target/web/stage`.

License
=======
This code is licensed under the [MIT License].

[sbt-web]:https://github.com/sbt/sbt-web
[yuicompressor]:http://yui.github.io/yuicompressor/
[MIT License]:http://opensource.org/licenses/MIT
[Sonatype releases]:https://oss.sonatype.org/content/repositories/releases/
