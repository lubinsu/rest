package com.eweise.api

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.duration
import duration._
object ApiBoot extends App {

  implicit val timeout = Timeout(10 seconds)

  val confHome = if (System.getenv("CONF_HOME") == "" | System.getenv("CONF_HOME") == null) "/appl/conf" else System.getenv("CONF_HOME")

  val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=2551")
    .withFallback(ConfigFactory.parseFile(new File(confHome + "/application.conf")))

  /**
   * All actors that want to belong to the same cluster need to use the same
   * ActorSystem name
   */
  implicit val actorSystem = ActorSystem("cluster-example", config)

  val apiRoutes = actorSystem.actorOf(Props[WebServiceActor], "api-routes")

  IO(Http) ! Http.Bind(apiRoutes, interface = "hadoop.slave2", port=9993)
}
