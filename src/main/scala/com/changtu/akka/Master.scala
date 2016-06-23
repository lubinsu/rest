// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by 6526 on 6/22/2016.
  */

import scala.collection.mutable.ListBuffer
import akka.actor.{Actor, ActorContext, ActorRef, Props}
import akka.routing.FromConfig
import com.changtu.utils.hbase.HBaseClient
import org.apache.hadoop.hbase.util.Bytes
import spray.routing.HttpService
import com.changtu.jsonprotocol.UserLabelDetailProtocol.detailFormat
import com.changtu.jsonprotocol.UserLabelJsonProtocol.labelFormat
import com.changtu.jsonprotocol.{UserLabelDetail, UserLabels}
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller

sealed class Helper(count: Int, replyTo: ActorRef) extends Actor {
  val perfectNumbers = new ListBuffer[Int]
  var nrOfResult = 0

  def receive = {
    case Result(num: Int, isPerfect: Boolean) =>
      nrOfResult += 1
      if (isPerfect)
        perfectNumbers += num
      if (nrOfResult == count)
        replyTo ! PerfectNumbers(perfectNumbers.toList)
  }
}

class Master extends Actor with HttpService {

  val Log = com.twitter.logging.Logger.get()
  //创建HBASE连接
  val labelStr = new HBaseClient(tablePath = "bi_user_label_string")
  val labelDtl = new HBaseClient(tablePath = "bi_user_label")


  // required as implicit value for the HttpService
  // included from SJService
  def actorRefFactory: ActorContext = context

  val worker = context.actorOf(Props[Worker].withRouter(FromConfig()), "workerRouter")
  // 获取某个用户的标签列表
  def getLabels(message: String): UserLabels = {
    val get = labelStr.getGet(message)

    if (labelStr.table.exists(get)) {
      val value = labelStr.get(get).getValue(Bytes.toBytes("labels"), Bytes.toBytes(""))
      val labels = Bytes.toString(value)
      UserLabels(message, labels, 0)
    } else {
      UserLabels(message, "", -1)
    }
  }

  // 获取标签的详细信息
  def getLabelDetails(message: String): UserLabelDetail = {

    val get = labelDtl.getGet(message)

    if (labelDtl.table.exists(get)) {
      val details = labelDtl.get(get)
      val labelInfo = "label_info"
      val labelCode = Bytes.toString(details.getValue(Bytes.toBytes(labelInfo), Bytes.toBytes("label_code")))
      val createDt = Bytes.toString(details.getValue(Bytes.toBytes(labelInfo), Bytes.toBytes("create_dt")))
      val modifyDt = Bytes.toString(details.getValue(Bytes.toBytes(labelInfo), Bytes.toBytes("modify_dt")))
      val status = Bytes.toString(details.getValue(Bytes.toBytes(labelInfo), Bytes.toBytes("status")))
      UserLabelDetail(message
        , labelCode
        , createDt
        , modifyDt
        , status
        , 0)
    } else {
      UserLabelDetail("", "", "", "", "", -1)
    }
  }

  // 根据条件过滤获取标签
  // handles the api path, we could also define these in separate files
  // this path response to get queries, and make a selection on the
  // media-type.
  // 配置路由
  // 查询用户对应的标签
  val route0 = path("userlabel" / Segment) {
    message => get {
      complete {
        getLabels(message)
      }
    }
  } ~
    // 查询详细信息
    path("userlabeldetail" / Segment) {
      message => get {
        complete {
          getLabelDetails(message)
        }
      }
    }

  def receive = runRoute(route0)
}
