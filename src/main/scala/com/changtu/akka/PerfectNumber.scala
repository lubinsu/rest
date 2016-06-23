// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by 6526 on 6/22/2016.
  */

import java.io.File

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Actor
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory

object PerfectNumber {

  def main(args: Array[String]): Unit = {

    val confHome = if (System.getenv("CONF_HOME") == "") "/appl/conf" else System.getenv("CONF_HOME")
    val system = ActorSystem("MasterApp", ConfigFactory.parseFile(new File(confHome + "/application.conf")).getConfig("RemoteSys"))

    system.actorOf(Props(new Actor() {
      val agent = context.system.actorOf(Props(new Agent()).withRouter(FromConfig()), "remoteMaster")
      dispatch()

      private def dispatch() = {
        val end = 100
        val start = 2
        val remotePaths = context.system.settings.config.getList("akka.actor.deployment./remoteMaster.target.nodes")
        val count = end - start + 1
        val piece = Math.round(count.toDouble / remotePaths.size()).toInt
        println("%s pieces per node".format(piece))
        var s = start
        while (end >= s) {
          var e = s + piece - 1
          if (e > end)
            e = end
          agent ! StartFind(s, e, self)
          s = e + 1
        }
        println(agent.path)
      }

      def receive = {
        case PerfectNumbers(list: List[Int]) =>
          println("\nFound Perfect Numbers:" + list.mkString(","))
          //system.shutdown()
      }
    }))
  }
}
