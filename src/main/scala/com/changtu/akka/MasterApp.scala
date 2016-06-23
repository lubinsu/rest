// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/22/2016.
  */
import java.io.File

import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSelection.toScala
import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.Cluster
import akka.kernel.Bootable
import com.changtu.rest.RestServiceActor

class Agent extends Actor {
  var master = context.system.actorSelection("/user/master")

  def receive = {
    case StartFind(start: Int, end: Int, replyTo: ActorRef) if start > 1 && end >= start =>
      master ! StartFind(start, end, sender)
  }
}

class MasterDaemon extends Bootable {
  val confHome = if (System.getenv("CONF_HOME") == "") "/appl/conf" else System.getenv("CONF_HOME")
  val system = ActorSystem("MasterApp", ConfigFactory.parseFile(new File(confHome + "/application.conf")).getConfig("RemoteSys"))

//  val system = ActorSystem("MasterApp", ConfigFactory.load.getConfig("remote"))
  val agent = system.actorOf(Props[Master], "master")
  Cluster(system).registerOnMemberUp(agent)

  def startup() = {}
  def shutdown() = {
    system.shutdown()
  }
}

object MasterApp {
  def main(args: Array[String]) {
    new MasterDaemon()
  }
}
