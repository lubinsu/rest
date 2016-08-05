package com.changtu.api

import java.io.File
import java.net.InetAddress

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.duration
import duration._
object ApiBoot extends App {

  if (args.length < 2) {
    System.err.println("Usage: com.changtu.api.ApiBoot <hostname> <port>")
    System.exit(1)
  }

  val Array(hostname, port) = args

  val seedHost = InetAddress.getLocalHost.getHostName


  implicit val timeout = Timeout(5 seconds)

  val confHome = if (System.getenv("CONF_HOME") == "" | System.getenv("CONF_HOME") == null) "/appl/conf" else System.getenv("CONF_HOME")

  val config = ConfigFactory.parseString(s"""
    akka {
      loglevel = INFO
      actor {
        provider = "akka.cluster.ClusterActorRefProvider"
      }
      remote {
        netty.tcp {
          hostname = "$seedHost"
          port = "2551"
        }
      }
    }
    """)
    .withFallback(ConfigFactory.parseFile(new File(confHome + "/application.conf")))

  /**
   * All actors that want to belong to the same cluster need to use the same
   * ActorSystem name
   */
  implicit val actorSystem = ActorSystem("cluster-example", config)

  val apiRoutes = actorSystem.actorOf(Props[WebServiceActor], "api-routes")

  IO(Http) ! Http.Bind(apiRoutes, interface = hostname, port=port.toInt)
}
