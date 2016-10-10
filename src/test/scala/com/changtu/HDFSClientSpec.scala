// Copyright (C) 2015-2016 the original author or authors.
package com.changtu

import java.net.URL

import com.changtu.util.redis.RedisUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source
import scala.util.Try


/**
  * Created by lubinsu on 2016/6/11.
  * 测试 HDFS client工具类
  */
class HDFSClientSpec extends FlatSpec with Matchers {

  def parseURL(url: String): Try[URL] = Try(new URL(url))

  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  def str2date(str: String): DateTime = DateTime.parse(str, formatter)
  "JSON test" should "get Object" in {

    val url = parseURL("http://www.changtu.com/chepiao/tonganqu-wenzhoushi.html") getOrElse new URL("http://duckduckgo.com")
    println(url.toString)
  }
}
