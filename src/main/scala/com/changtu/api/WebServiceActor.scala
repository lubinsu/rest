package com.changtu.api

import akka.actor.{Actor, ActorLogging}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Send
import akka.pattern._
import akka.util.Timeout
import com.changtu.core._
import com.changtu.service.BusScenic
import org.json4s.{DefaultFormats, Extraction, Formats}
import spray.http.MediaTypes
import spray.httpx.Json4sSupport
import spray.routing.HttpService
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable.ArrayBuffer
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

class WebServiceActor extends Actor with HttpService with ActorLogging {

  import JsonProtocol._

  implicit val timeout = Timeout(10 seconds)

  val actorRefFactory = context
  val mediator = DistributedPubSubExtension(context.system).mediator

  implicit val ec = ExecutionContext.Implicits.global

  val route =
    path("userlabel") {
      post {
        parameters('userId, 'labelCode) { (userId, labelCode) =>
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
              log.info("[userlabel] [SUCCESS] ".concat(compact(render(Extraction.decompose(BusScenic(userId, labelCode))))))
              complete(value.asInstanceOf[UserLabels])
            case Failure(e) =>
              log.error("[userlabel] [FAILED] ".concat(compact(render(Extraction.decompose(BusScenic(userId, labelCode))))))
              complete(ReturnMsg("BIG_DATA_ERR_00001", e.getMessage, ArrayBuffer.empty[ResultMsg]))
            case _ =>
              log.error("[userlabel] [ERROR] ".concat(compact(render(Extraction.decompose(BusScenic(userId, labelCode))))))
              complete(ReturnMsg("BIG_DATA_ERR_99999", "unknown error", ArrayBuffer.empty[ResultMsg]))
          }
        }
      }
    } ~ path("riskcontrol" / Segment) {
      case "register" =>
        post {
          entity(as[Register]) {
            case register: Register =>
              respondWithMediaType(MediaTypes.`application/json`) {
                onComplete(mediator ? Send("/user/backend-service", register, localAffinity = false)) {
                  case Success(value) =>
                    log.info("[riskcontrol/register] [SUCCESS] ".concat(compact(render(Extraction.decompose(register)))))
                    complete(value.asInstanceOf[ReturnMsg])
                  case Failure(e) =>
                    log.error("[riskcontrol/register] [FAILED] ".concat(compact(render(Extraction.decompose(register)))).concat(e.getMessage))
                    complete(ReturnMsg("BIG_DATA_ERR_00001", e.getMessage, ArrayBuffer.empty[ResultMsg]))
                  case _ =>
                    log.error("[riskcontrol/register] [ERROR] ".concat(compact(render(Extraction.decompose(register)))))
                    complete(ReturnMsg("BIG_DATA_ERR_99999", "unknown error", ArrayBuffer.empty[ResultMsg]))
                }
              }
          }
        }
      case "login" =>
        post {
          entity(as[Login]) {
            case login: Login =>
              respondWithMediaType(MediaTypes.`application/json`) {
                onComplete(mediator ? Send("/user/backend-service", login, localAffinity = false)) {
                  case Success(value) =>
                    log.info("[riskcontrol/login] [SUCCESS] ".concat(compact(render(Extraction.decompose(login)))))
                    complete(value.asInstanceOf[ReturnMsg])
                  case Failure(e) =>
                    log.error("[riskcontrol/login] [FAILED] ".concat(compact(render(Extraction.decompose(login)))).concat(e.getMessage))
                    complete(ReturnMsg("BIG_DATA_ERR_00001", e.getMessage, ArrayBuffer.empty[ResultMsg]))
                  case _ =>
                    log.error("[riskcontrol/login] [ERROR] ".concat(compact(render(Extraction.decompose(login)))))
                    complete(ReturnMsg("BIG_DATA_ERR_99999", "unknown error", ArrayBuffer.empty[ResultMsg]))
                }
              }
          }
        }
      case "ordersubmit" =>
        post {
          entity(as[OrderSubmit]) {
            case orderSubmit: OrderSubmit =>
              respondWithMediaType(MediaTypes.`application/json`) {
                onComplete(mediator ? Send("/user/backend-service", orderSubmit, localAffinity = false)) {
                  case Success(value) =>
                    log.info("[riskcontrol/ordersubmit] [SUCCESS] ".concat(compact(render(Extraction.decompose(orderSubmit)))))
                    complete(value.asInstanceOf[ReturnMsg])
                  case Failure(e) =>
                    log.error("[riskcontrol/ordersubmit] [FAILED] ".concat(compact(render(Extraction.decompose(orderSubmit)))).concat(e.getMessage))
                    complete(ReturnMsg("BIG_DATA_ERR_00001", e.getMessage, ArrayBuffer.empty[ResultMsg]))
                  case _ =>
                    log.error("[riskcontrol/ordersubmit] [ERROR] ".concat(compact(render(Extraction.decompose(orderSubmit)))))
                    complete(ReturnMsg("BIG_DATA_ERR_99999", "unknown error", ArrayBuffer.empty[ResultMsg]))
                }
              }
          }
        }
      case "payreturn" =>
        post {
          entity(as[PayReturn]) {
            case payReturn: PayReturn =>
              respondWithMediaType(MediaTypes.`application/json`) {
                onComplete(mediator ? Send("/user/backend-service", payReturn, localAffinity = false)) {
                  case Success(value) =>
                    log.info("[riskcontrol/payreturn] [SUCCESS] ".concat(compact(render(Extraction.decompose(payReturn)))))
                    complete(value.asInstanceOf[ReturnMsg])
                  case Failure(e) =>
                    log.error("[riskcontrol/payreturn] [FAILED] ".concat(compact(render(Extraction.decompose(payReturn)))).concat(e.getMessage))
                    complete(ReturnMsg("BIG_DATA_ERR_00001", e.getMessage, ArrayBuffer.empty[ResultMsg]))
                  case _ =>
                    log.error("[riskcontrol/payreturn] [ERROR] ".concat(compact(render(Extraction.decompose(payReturn)))))
                    complete(ReturnMsg("BIG_DATA_ERR_99999", "unknown error", ArrayBuffer.empty[ResultMsg]))
                }
              }
          }
        }

    }

  def receive = runRoute(route)

}
