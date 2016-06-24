package com.changtu.akka

import java.io.File

import akka.actor.{Actor, ActorContext, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import spray.routing.HttpService
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

/**
  * Created by 6526 on 6/24/2016.
  */
class PathSender extends Actor with HttpService {

  val Log = com.twitter.logging.Logger.get()
  def actorRefFactory: ActorContext = context

  val confHome = if (System.getenv("CONF_HOME") == "") "/appl/conf" else System.getenv("CONF_HOME")
  val system = ActorSystem("MasterApp", ConfigFactory.parseFile(new File(confHome + "/application.conf")).getConfig("MainDispatch"))

  val clusterClient = system.actorOf(Props[ClusterClient], "remoteMaster")
  implicit val timeout = Timeout(5.seconds)

  val route0 = path("userlabel" / Segment) {
    message => get {
      complete {
        println("Completed!!!")
        (clusterClient ? MessageFind(message, self)).mapTo[ResultMsg].value.mkString(",")
      }
    }
  }

  def receive: Actor.Receive = runRoute(route0)

}
