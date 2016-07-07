package com.eweise.api

import akka.actor.Actor
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Send
import akka.pattern._
import akka.util.Timeout
import com.changtu.jsonprotocol.UserLabels
import org.json4s.{DefaultFormats, Formats}
import spray.httpx.Json4sSupport
import spray.routing.HttpService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Web Server
  */
object JsonProtocol extends Json4sSupport {
  //  implicit def fooFormat: Formats = DefaultFormats
  implicit def json4sFormats: Formats = DefaultFormats
}

class WebServiceActor extends Actor with HttpService {

  import JsonProtocol._

  implicit val timeout = Timeout(10 seconds)

  val actorRefFactory = context
  val mediator = DistributedPubSubExtension(context.system).mediator
  implicit val ec = ExecutionContext.Implicits.global

  def receive = runRoute(route)

  val route =
    path("userlabel") {
      get {
        parameters('userId) { (userId) =>
          onComplete(mediator ? Send("/user/backend-service", userId, localAffinity = false)) {
            case Success(value) =>
              complete(value.asInstanceOf[UserLabels])
            case Failure(e) =>
              complete(e.getMessage)
            case _ =>
              complete("unknown error.")
          }
        }
      }
    }

}
