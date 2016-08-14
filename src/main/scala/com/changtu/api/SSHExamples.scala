package com.changtu.api

import com.changtu.core.SSH
import com.changtu.util.Logging

/**
  * Created by lubinsu on 8/11/2016.
  */
object SSHExamples extends App with Logging {

  val ssh = (cmd: String) => SSH("bigdata1", "hadoop", 22, cmd, args(0), "0(s6bXju")

  ssh("echo \"aaa\" >> ~/aa.log")

}
