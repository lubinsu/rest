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
package com.changtu.hbase

import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import org.apache.hadoop.hbase.client.ConnectionFactory

/**
  * Created by lubinsu on 2016/6/3.
  */
object HBaseUtils {
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
}
