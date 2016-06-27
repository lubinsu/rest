// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by 6526 on 6/22/2016.
  */

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.routing.FromConfig
import spray.routing.HttpService

sealed class Helper(msg: String, replyTo: ActorRef) extends Actor {

  def receive = {
    case ResultMsg(msg: String, replyTo: ActorRef) =>
        replyTo ! ResultMsg(msg: String, replyTo: ActorRef)
  }
}

class Master extends Actor with HttpService {

  val Log = com.twitter.logging.Logger.get()
  //创建HBASE连接
  //  val labelStr = new HBaseClient(tablePath = "bi_user_label_string")
  //  val labelDtl = new HBaseClient(tablePath = "bi_user_label")


  // required as implicit value for the HttpService
  // included from SJService
  def actorRefFactory: ActorContext = context

  val worker = context.actorOf(Props[Worker].withRouter(FromConfig()), "workerRouter")
  // 获取某个用户的标签列表
  //  def getLabels(message: String): String = {
  //    val get = labelStr.getGet(message)
  //
  //    if (labelStr.table.exists(get)) {
  //      val value = labelStr.get(get).getValue(Bytes.toBytes("labels"), Bytes.toBytes(""))
  //      val labels = Bytes.toString(value)
  //      labels
  //    } else {
  //      "None"
  //    }
  //  }

  // 根据条件过滤获取标签
  // handles the api path, we could also define these in separate files
  // this path response to get queries, and make a selection on the
  // media-type.
  // 配置路由
  // 查询用户对应的标签

  def receive = {

    case MessageFind(message: String, resultTo: ActorRef) =>
      //val helper = context.actorOf(Props(new Helper(message, resultTo)))
      //println("Master:" + message)
      println("Sender:" + resultTo)
      resultTo ! ResultMsg(message: String, resultTo: ActorRef)
      //worker.tell(ConsistentHashableEnvelope(WorkMsg(message,helper), message.toLong), helper)

  }
}
