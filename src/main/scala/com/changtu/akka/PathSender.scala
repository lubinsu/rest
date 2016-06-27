package com.changtu.akka

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import spray.routing.HttpService

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
  * Created by 6526 on 6/24/2016.
  */
class PathSender extends Actor with HttpService {

  val Log = com.twitter.logging.Logger.get()

  def actorRefFactory: ActorContext = context

  val confHome = if (System.getenv("CONF_HOME") == "") "/appl/conf" else System.getenv("CONF_HOME")
  val system = context.system
  //val system = ActorSystem("MasterApp", ConfigFactory.parseFile(new File(confHome + "/application.conf")).getConfig("MainDispatch"))

  implicit val timeout = Timeout(5.seconds)

  val route0 = path("userlabel" / Segment) {
    message => get {
      complete {
        println("Completed!!!")
        println("PathSender:" + message)
        val clusterClient = system.actorOf(Props[ClusterClient], "remoteSender")
        val future = clusterClient ? MessageFind(message, clusterClient)
        println("future:" + future)
        val rst = future.onComplete {
          case Success(ResultMsg(msg: String, replyTo: ActorRef)) =>
            println(msg)
            msg
          case Failure(error) =>
            println(error)
            error
          /*case result: String => "Got result " + result
          case result: ResultMsg =>
            println("Got result msg :" + result.msg)
            result.msg*/
          case _ => "None"
        }
        val result = Await.result(future, timeout.duration).asInstanceOf[ResultMsg]
        println(result.msg)
        result.msg
//        rst.toString
      }
    }
  }

  def receive: Actor.Receive = runRoute(route0)

}
