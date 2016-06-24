// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/22/2016.
  */

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.duration.DurationInt

object PerfectNumber extends App {

  // create our actor system with the name com.changtu.rest
  //implicit val system = ActorSystem("com-changtu-rest")
  val confHome = if (System.getenv("CONF_HOME") == "") "/appl/conf" else System.getenv("CONF_HOME")
  implicit val system = ActorSystem("MasterApp", ConfigFactory.parseFile(new File(confHome + "/application.conf")).getConfig("MainSys"))

  val service = system.actorOf(Props[PathSender], "changtu-rest-service")

  // IO requires an implicit ActorSystem, and ? requires an implicit timeout
  // Bind HTTP to the specified service.
  implicit val timeout = Timeout(5.seconds)
  val port = 9993
  IO(Http) ? Http.Bind(service, interface = "172.18.5.119", port = port)
}
