// Copyright (C) 2015-2016 the original author or authors.
package com.changtu

import java.net.URL

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Try


/**
  * Created by lubinsu on 2016/6/11.
  * 测试 HDFS client工具类
  */
class HDFSClientSpec extends FlatSpec with Matchers {

  def parseURL(url: String): Try[URL] = Try(new URL(url))

  "JSON test" should "get Object" in {

    val url = parseURL("garbage").map(_.getProtocol)
    println(url)
  }
}
