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

import akka.actor.Actor
import com.changtu.rest.UserLabelJsonProtocol._
import com.changtu.rest.UserLabelDetailProtocol._
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.client.{ConnectionFactory, Get}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import spray.httpx.SprayJsonSupport._
import spray.routing._

class RestServiceActor extends Actor with HttpService {

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
  if (admin.tableExists(labelString)) {
    println("Table exist.")
  } else {
    println("Table does not exist.")
    System.exit(-1)
  }

  val userLabelDetail = TableName.valueOf("bi_user_label")
  val userLabelDetailTable = connection.getTable(userLabelDetail)
  if (admin.tableExists(userLabelDetail)) {
    println("userLabelDetail exist.")
  } else {
    println("userLabelDetail does not exist.")
    System.exit(-1)
  }

  //val result = table.get(new Get(Bytes.toBytes("1735909")))
  //println(Bytes.toString(result.value()))
  //admin.close()

  // required as implicit value for the HttpService
  // included from SJService
  def actorRefFactory = context

  // we don't create a receive function ourselve, but use
  // the runRoute function from the HttpService to create
  // one for us, based on the supplied routes.
  def receive = runRoute(route0)

  // handles the api path, we could also define these in separate files
  // this path response to get queries, and make a selection on the
  // media-type.
  // 配置路由
  val route0 = path("userlabel" / Segment) {
    message => get {
      complete {
        if (labelTable.exists(new Get(Bytes.toBytes(message)))) {
          val labels = Bytes.toString(labelTable.get(new Get(Bytes.toBytes(message))).value())
          UserLabels(message, labels, 0)
        } else {
          UserLabels(message, "", -1)
        }
      }
    }
  } ~
    path("userlabeldetail" / Segment) {
      message => get {

        complete {

          if (userLabelDetailTable.exists(new Get(Bytes.toBytes(message)))) {
            val details = userLabelDetailTable.get(new Get(Bytes.toBytes(message)))
            val labelCode = Bytes.toString(details.getValue(Bytes.toBytes("label_info"), Bytes.toBytes("label_code")))
            val createDt = Bytes.toString(details.getValue(Bytes.toBytes("label_info"), Bytes.toBytes("create_dt")))
            val modifyDt = Bytes.toString(details.getValue(Bytes.toBytes("label_info"), Bytes.toBytes("modify_dt")))
            val status = Bytes.toString(details.getValue(Bytes.toBytes("label_info"), Bytes.toBytes("status")))
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
      }
    }
}
