package com.changtu.api

/**
  * Created by lubinsu on 8/8/2016.
  */

import akka.actor.ActorSystem
import akka.io.IO
import akka.pattern.ask
import com.changtu.core.UserLabels
import com.changtu.util.Logging
import com.changtu.util.host.{AES, Configuration, SSH}
import spray.can.Http
import spray.client.pipelining._
import spray.util._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object ApiCheck extends App with Logging {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("simple-spray-client")

  import system.dispatcher

  // execution context for futures below
  logger.info("Requesting the labels from Changtu rest API...")

  import JsonProtocol._

  val pipeline = sendReceive ~> unmarshal[UserLabels]
  val Array(leaders, followers) = args

  leaders.split(",").foreach(p => {
    val host = p.split(":")(0)
    val responseFuture = pipeline {
      Post("http://".concat(host).concat(":9993/userlabel?userId=5236317&labelCode=coach_spot_class"))
    }
    handleFuture(responseFuture, host)
    //errorApi += host
  })

  /**
    * 重启leader
    *
    * @param hosts 主机名，多台主机，用逗号分隔
    */
  def restartLeader(hosts: String): Unit = {

    hosts.split(",").foreach(p => {
      val host = p.split(":")(0)
      val port = p.split(":")(1)
      // 密码解密
      val password = AES.decrypt(Configuration("passwd").getString(host.concat("-hadoop")), "secretKey.changtu.com") match {
        case Success(encrypted) =>
          encrypted.asInstanceOf[String]
        case Failure(e) =>
          logger.error(e.getMessage)
          ""
      }
      val ssh = (cmd: String) => SSH(host, "hadoop", port.toInt, cmd, "", password)
      logger.info("restarting flower host : ".concat(host))
      ssh("ps -ef | grep com.changtu.api.ApiBoot | grep 9993 | grep -v grep | awk '{print $2}' | xargs kill")
      Thread.sleep(1000)
      ssh("nohup java -Djava.ext.dirs=/appl/scripts/e-business/rest/target/lib -classpath /appl/scripts/e-business/rest/target/rest-1.1.jar com.changtu.api.ApiBoot ".concat(host).concat(" 9993 > /appl/scripts/e-business/rest/out.follower.$$.log 2>&1 &"))
      //ssh("nohup")
    })

  }

  /**
    * 重启follower
    *
    * @param hosts 主机名，多台主机，用逗号分隔
    */
  def restartFlower(hosts: String): Unit = {

    hosts.split(",").foreach(p => {
      val host = p.split(":")(0)
      val port = p.split(":")(1)
      // 密码解密
      val password = AES.decrypt(Configuration("passwd").getString(host.concat("-hadoop")), "secretKey.changtu.com") match {
        case Success(encrypted) =>
          encrypted.asInstanceOf[String]
        case Failure(e) =>
          logger.error(e.getMessage)
          ""
      }
      val ssh = (cmd: String) => SSH(host, "hadoop", port.toInt, cmd, "", password)
      logger.info("restarting flower host : ".concat(host))
      ssh("ps -ef | grep com.changtu.service.BackendServiceBoot | grep 4444 | grep -v grep | awk '{print $2}' | xargs kill")
      Thread.sleep(500)
      ssh("nohup java -Djava.ext.dirs=/appl/scripts/e-business/rest/target/lib -classpath /appl/scripts/e-business/rest/target/rest-1.1.jar com.changtu.service.BackendServiceBoot 4444 > /appl/scripts/e-business/rest/out.follower.$$.log 2>&1 &")
      //ssh("nohup")
    })
  }

  /**
    * 处理异常接口
    *
    * @param responseFuture 响应线程
    */
  def handleFuture(responseFuture: Future[UserLabels], host: String): Unit = {
    responseFuture onComplete {
      case Success(labels: UserLabels) =>
        logger.info("The API call was successful and get the right labels from ".concat(host).concat(":").concat(labels.asInstanceOf[UserLabels].labels))
        shutdown()
      case Success(somethingUnexpected) =>
        logger.warn("The API call was successful but returned something unexpected on : ".concat(host).concat(":").concat(somethingUnexpected.toString))
        shutdown()
      case Failure(error) =>
        logger.error("Couldn't get labels from ".concat(host).concat(":".concat(error.toString)))
        restartFlower(followers)
        shutdown()
    }
  }

  def shutdown(): Unit = {
    IO(Http).ask(Http.CloseAll)(1.second).await
    system.shutdown()
  }
}
