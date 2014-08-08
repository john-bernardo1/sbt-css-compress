package net.ground5hark.sbt.css

import java.io.{IOException, InputStreamReader, FileInputStream, OutputStreamWriter, FileOutputStream, Closeable}

import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.web.{PathMapping, SbtWeb}
import com.yahoo.platform.yui.compressor.CssCompressor
import sbt.Keys._
import sbt._
import collection.mutable

object Import {
  val cssCompress = TaskKey[Pipeline.Stage]("css-compress", "Runs the CSS compressor on the assets in the pipeline")

  object CssCompress {
    val suffix = SettingKey[String]("css-compress-suffix", "Suffix to append to compressed files, default: \".min.css\"")
    val parentDir = SettingKey[String]("css-compress-parent-dir", "Parent directory name where compressed CSS will go, default: \"css-compress\"")
    val lineBreak = SettingKey[Int]("css-compress-line-break", "Position in the compressed output at which to break out a new line, default: -1 (never)")
  }
}

class UnminifiedCssFileFilter(suffix: String) extends FileFilter {
  override def accept(file: File): Boolean =
    !HiddenFileFilter.accept(file) && !file.getName.endsWith(suffix) && file.getName.endsWith(".css")
}

object util {
  def withoutExt(name: String): String = name.substring(0, name.lastIndexOf("."))
  def withParent(f: File): String = f.getParentFile.getName + "/" + f.getName
}

object SbtCssCompress extends AutoPlugin {
  override def requires = SbtWeb

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import autoImport._
  import CssCompress._

  override def projectSettings = Seq(
    suffix := ".min.css",
    parentDir := "css-compress",
    lineBreak := -1,
    includeFilter in cssCompress := new UnminifiedCssFileFilter(suffix.value),
    cssCompress := compress.value
  )

  private def invokeCompressor(src: File, target: File, lineBreak: Int): Unit = {
    val openStreams = mutable.ListBuffer.empty[Closeable]
    try {
      val reader = new InputStreamReader(new FileInputStream(src))
      openStreams += reader
      val compressor = new CssCompressor(reader)
      val writer = new OutputStreamWriter(new FileOutputStream(target), "UTF-8")
      openStreams += writer
      compressor.compress(writer, lineBreak)
    } catch {
      case t: Throwable => throw t
    } finally {
      // Close silently
      openStreams.foreach { stream =>
        try {
          stream.close()
        } catch {
          case e: IOException =>
        }
      }
    }
  }

  private def compress: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings: Seq[PathMapping] =>
      val targetDir = webTarget.value / parentDir.value
      val compressMappings = mappings.view.filter(m => (includeFilter in cssCompress).value.accept(m._1)).toMap

      val runCompressor = FileFunction.cached(streams.value.cacheDirectory / parentDir.value, FilesInfo.lastModified) {
        files =>
          files.map { f =>
            val outputFileSubPath = s"${util.withoutExt(compressMappings(f))}${suffix.value}"
            val outputFile = targetDir / outputFileSubPath
            IO.createDirectory(outputFile.getParentFile)
            streams.value.log.info(s"Compressing file ${compressMappings(f)}")
            invokeCompressor(f, outputFile, lineBreak.value)
            outputFile
          }
      }

      val compressed = runCompressor(compressMappings.keySet).map { outputFile =>
        (outputFile, util.withParent(outputFile))
      }.toSeq

      compressed ++ mappings.filter {
        // Handle duplicate mappings
        case (mappingFile, mappingName) =>
          val include = compressed.filter(_._2 == mappingName).isEmpty
          if (!include)
            streams.value.log.warn(s"css-compressor encountered a duplicate mapping for $mappingName and will " +
              "prefer the css-compressor version instead. If you want to avoid this, make sure you aren't " +
              "including minified and non-minified sibling assets in the pipeline.")
          include
      }
  }
}
