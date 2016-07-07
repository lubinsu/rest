package com.eweise.service

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration
import duration._

/**
 * Pass in the port as first argument so that we can run many backend services
 */
object BackendServiceBoot extends App {

  implicit val timeout = Timeout(10 seconds)

  val confHome = if (System.getenv("CONF_HOME") == "" | System.getenv("CONF_HOME") == null) "/appl/conf" else System.getenv("CONF_HOME")
  val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${args(0)}")
    .withFallback(ConfigFactory.parseFile(new File(confHome + "/application.conf")))

  implicit val actorSystem = ActorSystem("cluster-example", config)
  actorSystem.actorOf(Props[BackendServiceActor], "backend-service")
}
