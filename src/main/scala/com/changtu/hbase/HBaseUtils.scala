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
