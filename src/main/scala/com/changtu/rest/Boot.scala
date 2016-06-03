package com.changtu.rest

/**
  * Created by lubinsu on 2016/6/2.
  */

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

object Boot extends App {

  // create our actor system with the name com.changtu.rest
  implicit val system = ActorSystem("com-changtu-rest")
  val service = system.actorOf(Props[RestServiceActor], "sj-rest-service")

  // IO requires an implicit ActorSystem, and ? requires an implicit timeout
  // Bind HTTP to the specified service.
  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(service, interface = "172.18.5.119", port = 9992)
}