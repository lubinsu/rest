package com.changtu

import com.changtu.hdfs.HDFSClient
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lubinsu on 2016/6/11.
  */
class HDFSClientSpec extends FlatSpec with Matchers {

  "Result" should "equal(132335)" in {
    HDFSClient.countLines("hdfs://nameservice1/user/hadoop/behavior/20160612/09") should be(132335)
  }
}
