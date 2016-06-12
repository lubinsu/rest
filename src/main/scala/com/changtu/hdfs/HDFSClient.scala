package com.changtu.hdfs

/**
  * Created by lubinsu on 2016/6/8.
  */
import java.io._

import com.twitter.logging.Logger
import org.apache.commons.io.IOUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, _}
import org.apache.hadoop.io.compress.CompressionCodecFactory

abstract class AbstractFSClient {

  def getLines(path: String): Iterable[String] = {
    iterator(path).toIterable
  }

  def getLines(parent: String, child1: String, child2: String): Iterable[String] = {
    getLines(composeAbsPath(parent, List(child1, child2)))
  }

  def scanLines(path: String, cb: (String => Any)) {
    for (line <- iterator(path)) {
      cb(line)
    }
  }

  // Safe version of scanLines
  def scanOrElse(path: String, cb: (String) => Any, failure: (FileNotFoundException => Any) = (_) => Nil): Unit = {
    try {
      scanLines(path, cb)
    } catch {
      case e: FileNotFoundException =>
        Logger.get().error("No available data in the path " + path)
        failure(e)
    }
  }

  def countLines(path: String): Long = {
    var n = 0l
    def cb(line: String) = n += 1
    scanOrElse(path, cb)
    n
  }

  def iterator(path: String): Iterator[String]

  def iterator(parent: String, child: String): Iterator[String] = {
    iterator(composeAbsPath(parent, child))
  }

  def composeAbsPath(parent: String, child: String): String = {
    require(parent.startsWith("/"), "The parent path " + parent + " must specify an absolute path")

    val relChildPath = if (child.startsWith("/")) child.substring(1, child.length()) else child

    if (parent.endsWith("/")) {
      parent + relChildPath
    } else {
      parent + "/" + relChildPath
    }
  }

  def composeAbsPath(parent: String, children: Iterable[String]): String = {
    children.toList match {
      case Nil => parent
      case head :: Nil => composeAbsPath(parent, head)
      case head :: tail => composeAbsPath(composeAbsPath(parent, head), tail)
    }
  }

  def composeAbsPath(parent: String, child1: String, child2: String): String =
    composeAbsPath(parent, List(child1, child2))
}

object HDFSClient extends AbstractFSClient {
  val conf = new Configuration()
  // 加载HADOOP配置文件
  try {
    conf.addResource(new Path("E:\\conf\\hdfs-site.xml"))
    conf.addResource(new Path("E:\\conf\\core-site.xml"))
    conf.addResource(new Path("E:\\conf\\yarn-site.xml"))
    conf.addResource(new Path("E:\\conf\\mapred-site.xml"))
  } catch {
    case e: IllegalArgumentException =>
      conf.addResource(new Path("/appl/conf/hdfs-site.xml"))
      conf.addResource(new Path("/appl/conf/core-site.xml"))
      conf.addResource(new Path("/appl/conf/yarn-site.xml"))
      conf.addResource(new Path("/appl/conf/mapred-site.xml"))
  }

  Logger.get().info("HDFS configuration properties: " + conf.iterator())

  override def iterator(path: String): Iterator[String] = {
    readLines(new Path(path), conf)
  }

  private def readLines(location: Path, conf: Configuration): Iterator[String] = {
    val fileSystem = FileSystem.get(location.toUri, conf)
    val factory = new CompressionCodecFactory(conf)
    val items = fileSystem.listStatus(location)
    if (items == null) return Iterator.empty
    val results = for (item <- items if !item.getPath.getName.startsWith("_")) yield {

      // check if we have a compression codec we need to use
      val codec = factory.getCodec(item.getPath)

      val stream = if (codec != null) {
        codec.createInputStream(fileSystem.open(item.getPath))
      } else {
        fileSystem.open(item.getPath)
      }

      val writer = new StringWriter()
      IOUtils.copy(stream, writer, "UTF-8")
      val raw = writer.toString
      raw.split("\n")
    }
    results.flatten.toIterator
  }
}
