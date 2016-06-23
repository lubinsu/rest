// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/22/2016.
  */

import java.io.File

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object PerfectNumber {

  def main(args: Array[String]): Unit = {

    val confHome = if (System.getenv("CONF_HOME") == "") "/appl/conf" else System.getenv("CONF_HOME")
    val system = ActorSystem("MasterApp", ConfigFactory.parseFile(new File(confHome + "/application.conf")).getConfig("RemoteSys"))

    system.actorOf(Props(new Actor() {
      context.system.actorOf(Props[ClusterClient], "remoteMaster") ! StartFind(2, 100, self)

      def receive = {
        case PerfectNumbers(list: List[Int]) =>
          println("\nFound Perfect Numbers:" + list.mkString(","))
          system.shutdown()
      }
    }))

  }
}
