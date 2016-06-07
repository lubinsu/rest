// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.changtu.rest

/**
  * Created by lubinsu on 2016/6/2.
  * Spary for rest api
  * 使用Spary提供rest服务接口，以此作为
  */

import akka.actor.{Actor, ActorContext}
import com.changtu.jsonprotocol.UserLabelDetailProtocol.detailFormat
import com.changtu.jsonprotocol.UserLabelJsonProtocol.labelFormat
import com.changtu.jsonprotocol.{UserLabelDetail, UserLabelDetailProtocol, UserLabelJsonProtocol, UserLabels}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.client.{ConnectionFactory, Get, Table}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.routing.HttpService

class RestServiceActor extends Actor with HttpService {

  val Log = com.twitter.logging.Logger.get()
  //创建HBASE连接
  val config = HBaseConfiguration.create()

  try {
    config.addResource(new Path("E:\\conf\\hbase-site.xml"))
    config.addResource(new Path("E:\\conf\\core-site.xml"))
  } catch {
    case e: IllegalArgumentException =>
      config.addResource(new Path("/appl/conf/hbase-site.xml"))
      config.addResource(new Path("/appl/conf/core-site.xml"))
  }

  val connection = ConnectionFactory.createConnection(config)
  val admin = connection.getAdmin


  val labelString = TableName.valueOf("bi_user_label_string")
  val labelTable = connection.getTable(labelString)
  if (!admin.tableExists(labelString)) {
    System.exit(-1)
  }

  val userLabelDetail = TableName.valueOf("bi_user_label")
  val userLabelDetailTable = connection.getTable(userLabelDetail)
  if (admin.tableExists(userLabelDetail)) {
    System.exit(-1)
  }

  // required as implicit value for the HttpService
  // included from SJService
  def actorRefFactory: ActorContext = context

  // we don't create a receive function ourselve, but use
  // the runRoute function from the HttpService to create
  // one for us, based on the supplied routes.
  def receive: Actor.Receive = runRoute(route0)


  // 获取某个用户的标签列表
  def getLabels(labelTable: Table, message: String): UserLabels = {
    if (labelTable.exists(new Get(Bytes.toBytes(message)))) {
      val labels = Bytes.toString(labelTable.get(new Get(Bytes.toBytes(message))).value())
      UserLabels(message, labels, 0)
    } else {
      UserLabels(message, "", -1)
    }
  }

  // 获取标签的详细信息
  def getLabelDetails(userLabelDetailTable: Table, message: String): UserLabelDetail = {

    if (userLabelDetailTable.exists(new Get(Bytes.toBytes(message)))) {
      val details = userLabelDetailTable.get(new Get(Bytes.toBytes(message)))
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
        getLabels(labelTable, message)
      }
    }
  } ~
    // 查询详细信息
    path("userlabeldetail" / Segment) {
      message => get {
        complete {
          getLabelDetails(userLabelDetailTable, message)
        }
      }
    }
}
