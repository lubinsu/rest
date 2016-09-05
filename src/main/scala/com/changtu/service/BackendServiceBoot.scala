package com.changtu.service

import java.io.File
import java.net.InetAddress

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.changtu.api.ApiBoot._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration
import duration._

/**
 * Pass in the port as first argument so that we can run many backend services
 */
object BackendServiceBoot extends App {

  if (args.length < 1) {
    System.err.println("Usage: com.changtu.service.BackendServiceBoot <port>")
    System.exit(1)
  }

  implicit val timeout = Timeout(5.seconds)
  val port = args(0)
  val hostName = InetAddress.getLocalHost.getHostName
  val confHome = if (System.getenv("CONF_HOME") == "" | System.getenv("CONF_HOME") == null) "/appl/conf" else System.getenv("CONF_HOME")
  val config = ConfigFactory.parseString(s"""
    akka {
      loglevel = INFO
      actor {
        provider = "akka.cluster.ClusterActorRefProvider"
      }
      remote {
        netty.tcp {
          hostname = "$hostName"
          port = "$port"
        }
      }
    }
    """)
    .withFallback(ConfigFactory.parseFile(new File(confHome + "/application.conf")))

  implicit val actorSystem = ActorSystem("cluster-risk-control", config)
  actorSystem.actorOf(Props[BackendServiceActor], "backend-service")
}
