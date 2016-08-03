// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.rest

/**
  * Created by lubinsu on 2016/6/2.
  * Spary for rest api
  * 使用Spary提供rest服务接口，以此作为
  */

import akka.actor.{Actor, ActorContext}
import akka.cluster.Cluster
import com.changtu.jsonprotocol.UserLabelDetailProtocol.detailFormat
import com.changtu.jsonprotocol.UserLabelJsonProtocol.labelFormat
import com.changtu.jsonprotocol.{UserLabelDetail, UserLabels}
import com.changtu.util.hbase.HBaseClient
import org.apache.hadoop.hbase.util.Bytes
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.routing.HttpService

class RestServiceActor extends Actor with HttpService {

  val Log = com.twitter.logging.Logger.get()
  //创建HBASE连接
  val labelStr = new HBaseClient(tablePath = "bi_user_label_string")
  val labelDtl = new HBaseClient(tablePath = "bi_user_label")


  // required as implicit value for the HttpService
  // included from SJService
  def actorRefFactory: ActorContext = context

  val cluster = Cluster(context.system)

  // we don't create a receive function ourselve, but use
  // the runRoute function from the HttpService to create
  // one for us, based on the supplied routes.
  def receive: Actor.Receive = runRoute(route0)


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
  /* ~
      // 查询详细信息
      path("label" / Segment) {
        message => get {
          complete {
            getLabelDetails(userLabelDetailTable, message)
          }
        }
      }*/
}
