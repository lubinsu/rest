package com.changtu.api

import akka.actor.{Actor, ActorLogging}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Send
import akka.pattern._
import akka.util.Timeout
import com.changtu.core.UserLabels
import com.changtu.service.BusScenic
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

class WebServiceActor extends Actor with HttpService with ActorLogging{

  import JsonProtocol._

  implicit val timeout = Timeout(10 seconds)

  val actorRefFactory = context
  val mediator = DistributedPubSubExtension(context.system).mediator
  implicit val ec = ExecutionContext.Implicits.global

  def receive = runRoute(route)

  val route =
    path("userlabel") {
      get {
        parameters('userId, 'labelCode) { (userId, labelCode) =>
          log.info("hello".concat(labelCode))
          /** 1. [[akka.contrib.pattern.DistributedPubSubMediator.Send]] -
           * The message will be delivered to one recipient with a matching path, if any such
           * exists in the registry. If several entries match the path the message will be sent
           * via the supplied `routingLogic` (default random) to one destination. The sender of the
           * message can specify that local affinity is preferred, i.e. the message is sent to an actor
           * in the same local actor system as the used mediator actor, if any such exists, otherwise
           * route to any other matching entry. A typical usage of this mode is private chat to one
           * other user in an instant messaging application. It can also be used for distributing
           * tasks to registered workers, like a cluster aware router where the routees dynamically
           * can register themselves.
           * */
          onComplete(mediator ? Send("/user/backend-service", BusScenic(userId, labelCode), localAffinity = false)) {
            case Success(value) =>
              complete(value.asInstanceOf[UserLabels])
            case Failure(e) =>
              complete(e.getMessage.concat(" when getting userId:").concat(userId))
            case _ =>
              complete("unknown error.".concat(" when getting userId:").concat(userId))
          }
        }
      }
    }

}
