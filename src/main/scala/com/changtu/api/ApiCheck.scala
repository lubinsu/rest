package com.changtu.api

/**
  * Created by lubinsu on 8/8/2016.
  */

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import com.changtu.core.UserLabels
import com.changtu.util.Logging
import spray.can.Http
import spray.client.pipelining._
import spray.util._
import scala.sys.process._

import scala.concurrent.duration._
import scala.util.{Failure, Success}


object ApiCheck extends App with Logging {

  /**
    *
    * @param hosts
    */
  def restartLeader(hosts: String) : Unit = {

  }

  /**
    *
    * @param hosts
    */
  def restartFlower(hosts: String) : Unit = {

  }

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("simple-spray-client")

  import system.dispatcher

  // execution context for futures below
  logger.info("Requesting the labels from Changtu rest API...")

  import JsonProtocol._

  val pipeline = sendReceive ~> unmarshal[UserLabels]

  val responseFuture = pipeline {
    Get("http://bigdata0:9993/userlabel?userId=5236317&labelCode=coach_spot_class")
  }
  responseFuture onComplete {
    case Success(labels: UserLabels) =>
      logger.info("The API call was successful and get the right labels".concat(labels.asInstanceOf[UserLabels].labels))
      shutdown()
    case Success(somethingUnexpected) =>
      logger.warn("The API call was successful but returned something unexpected: '{}'.".concat(somethingUnexpected.toString))
      shutdown()
    case Failure(error) =>
      logger.error("Couldn't get labels".concat(error.toString))
      "/appl/scripts/restartLeader.sh \"hadoop.slave2:11017 bigdata0:22 bigdata1:22 hadoop.slave1:11017\"".!
      shutdown()
  }

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }
}
