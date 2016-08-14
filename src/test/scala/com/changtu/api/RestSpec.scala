package com.changtu.api

import akka.actor.ActorSystem
import akka.io.IO
import com.changtu.core.UserLabels
import com.changtu.util.Logging
import org.scalatest.{FreeSpec, Matchers}
import spray.can.Http
import spray.client.pipelining._
import akka.pattern.ask
import spray.util._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by lubinsu on 8/8/2016.
  * 测试rest服务正常
  */
class RestSpec extends FreeSpec with Matchers with Logging {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("simple-spray-client")

  import system.dispatcher



  import JsonProtocol._

  val pipeline = sendReceive ~> unmarshal[UserLabels]

  val responseFuture = pipeline {
    Get("http://bigdata0:9993/userlabel?userId=52363&labelCode=coach_spot_class")
  }

  "Labels api should return UserLabels json string" in {
    // execution context for futures below
    logger.info("Requesting the elevation of Mt. Everest from Googles Elevation API...")
    responseFuture onComplete {

      case Success(labels: UserLabels) =>
        logger.info(labels.asInstanceOf[UserLabels].labels)
        shutdown()
      case Success(somethingUnexpected) =>
        logger.warn("The API call was successful but returned something unexpected: '{}'.".concat(somethingUnexpected.toString))
        shutdown()
      case Failure(error) =>
        logger.error("Couldn't get labels".concat(error.toString))
        shutdown()
    }
}

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }

}
