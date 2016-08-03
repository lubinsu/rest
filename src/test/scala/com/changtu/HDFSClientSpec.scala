// Copyright (C) 2015-2016 the original author or authors.
package com.changtu

import com.changtu.util.hdfs.HDFSUtils
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lubinsu on 2016/6/11.
  * 测试 HDFS client工具类
  */
class HDFSClientSpec extends FlatSpec with Matchers {

  "HDFS client" should "create a file" in {
    HDFSUtils.createDirectory("/user/hadoop/test", deleteF = true) should be(true)
    HDFSUtils.release()
  }
}
