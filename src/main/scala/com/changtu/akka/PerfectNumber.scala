// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/22/2016.
  */

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.io.IO
import spray.can.Http

/*object PerfectNumber extends App {

  // create our actor system with the name com.changtu.rest
  val confHome = if (System.getenv("CONF_HOME") == "") "/appl/conf" else System.getenv("CONF_HOME")
  implicit val system = ActorSystem("MasterApp"/*, ConfigFactory.parseFile(new File(confHome + "/application.conf")).getConfig("MainSys")*/)

  val service = system.actorOf(Props[PathSender], "remoteMaster")

  // IO requires an implicit ActorSystem, and ? requires an implicit timeout
  // Bind HTTP to the specified service.
  implicit val timeout = Timeout(5.seconds)
  val port = 9993
  IO(Http) ? Http.Bind(service, interface = "172.18.5.119", port = port)
}*/


object Boot extends App {
  implicit val system = ActorSystem("spray-sample-system")

  /* Use Akka to create our Spray Service */
  val service = system.actorOf(Props[PerfectNumber], "spray-sample-service")

  /* and bind to Akka's I/O interface */
  IO(Http) ! Http.Bind(service, system.settings.config.getString("app.interface"), system.settings.config.getInt("app.port"))

}

/* Our Server Actor is pretty lightweight; simply mixing in our route trait and logging */
class PerfectNumber extends Actor with PathSender with ActorLogging {

  def actorRefFactory = context

  def receive = runRoute(route0)
}
