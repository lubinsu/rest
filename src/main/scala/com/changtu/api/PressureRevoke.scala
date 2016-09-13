package com.changtu.api

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import com.changtu.core.ResultMsg
import com.changtu.util.Logging
import org.joda.time.DateTime
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods._
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import spray.util._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by lubinsu on 9/7/2016.
  */


object PressureRevoke extends App with Logging {

  case class ReturnMsg(status: String, comment: String, resultSet: List[ResultMsg])

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("simple-spray-client")

  import JsonProtocol._
  import system.dispatcher

  val pipeline = sendReceive ~> unmarshal[ReturnMsg]
  var cnt = 0

  logger.info("[START]".concat(DateTime.now.toString("yyyy-MM-dd HH:mm:ss:SSS")))
  for (a <- 1.toLong to args(0).toLong) {

    cnt % 2000 match {
      case 0 => Thread.sleep(1000)
      case _ =>
    }

    val host = a % 2 match {
      case 0 => "172.19.0.95"
      case 1 => "172.19.0.110"
    }

    val url = a % 4 match {
      case 0 => "/riskcontrol/login"
      case 1 => "/riskcontrol/register"
      case 2 => "/riskcontrol/ordersubmit"
      case 3 => "/riskcontrol/payreturn"
    }

    val json = a % 4 match {
      case 0 => "{\"publicParam\": {\"actionId\": \"11\",\"sercetId\": \"11\",\"signature\": \"12\",\"clientIp\": \"172.18.5.119\",\"sourceLoc\": \"1\"},\"loginSource\": \"pc\",\"smsMobile\": \"13001212547\",\"userId\": \"5236317\"}"
      case 1 => "{\"publicParam\": {\"actionId\": \"11\",\"sercetId\": \"11\",\"signature\": \"12\",\"clientIp\": \"172.18.5.119\",\"sourceLoc\": \"1\"},\"regSource\": \"xyz\",\"smsMobile\": \"13001212547\"}"
      case 2 => "{\"publicParam\": {\"actionId\": \"11\",\"sercetId\": \"11\",\"signature\": \"12\",\"clientIp\": \"172.18.5.119\",\"sourceLoc\": \"1\"},\"userId\": \"8755154\",\"channelId\": \"1\",\"startDate\": \"2016-09-05 12:30:00\",\"startCityId\": \"1\",\"endCityId\": \"63\",\"contact\": \"13001212547\"}"
      case 3 => "{\"publicParam\": {\"actionId\": \"11\",\"sercetId\": \"11\",\"signature\": \"12\",\"clientIp\": \"172.18.5.119\",\"sourceLoc\": \"1\"},\"userId\": \"5236317\",\"orderId\": \"14846310\",\"startCityId\": \"1\",\"payStatus\": \"10\",\"buyerEmail\": \"yc115@xcslw.com\"}"
    }

    val response = pipeline(HttpRequest(method = HttpMethods.POST, uri = "http://".concat(host).concat(":9993").concat(url), entity = HttpEntity(ContentTypes.`application/json`, json)))

    handleFuture(response, host)
  }

  /**
    * 处理异常接口
    *
    * @param responseFuture 响应线程
    */
  def handleFuture(responseFuture: Future[ReturnMsg], host: String): Unit = {
    responseFuture onComplete {

      case Success(returnMsg: ReturnMsg) =>
        logger.info("[SUCCESS]".concat("[".concat(host).concat("]")).concat(compact(render(Extraction.decompose(returnMsg)))))
        count()
      case Success(somethingUnexpected) =>
        logger.error("[SUCCESS]")
        count()
      case Failure(error) =>
        logger.error("[ERROR]".concat(error.getMessage))
        count()
    }
  }

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(10.second).await
    system.shutdown()
  }

  def count(): Unit = {
    cnt += 1
    if(cnt == args(0).toLong) {
      logger.info("[END]".concat(DateTime.now.toString("yyyy-MM-dd HH:mm:ss:SSS")))
      shutdown()
    }
  }
}
