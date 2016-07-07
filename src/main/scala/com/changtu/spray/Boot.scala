// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.spray

/**
  * Created by lubinsu on 2016/6/2.
  */
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

import scala.concurrent.duration.DurationInt

object Boot extends App {

  // create our actor system with the name com.changtu.rest
  implicit val system = ActorSystem("com-changtu-rest")
  val appInterface = system.settings.config.getString("app.interface")

  val appPort = system.settings.config.getInt("app.port")

  val service = system.actorOf(Props[MainCluster], "changtu-rest-service")
  // IO requires an implicit ActorSystem, and ? requires an implicit timeout
  // Bind HTTP to the specified service.
  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = appInterface, port = appPort)
}
class IoActor extends Actor with IoService with ActorLogging{
  def actorRefFactory = context
  def receive = runRoute(serviceRoute)
}