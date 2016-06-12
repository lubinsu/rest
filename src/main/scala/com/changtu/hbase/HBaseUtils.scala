package com.changtu.hbase

import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.client.{ConnectionFactory, Get, Result, Scan}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}

import scala.collection.JavaConversions._

/**
  * Created by lubinsu on 2016/6/3.
  */
object HBaseUtils {

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
  val scan = new Scan()
  val scanner = userLabelDetailTable.getScanner(scan)
  if (!admin.tableExists(userLabelDetail)) {
    System.exit(-1)
  }

  def getHtable = labelTable
  //
  def getScan(columnFamilies: TraversableOnce[String]): Scan = {
    val scan = new Scan
    scan.setCaching(5000)
    columnFamilies.foreach(fam => scan.addFamily(Bytes.toBytes(fam)))
    scan
  }


  def getGet(rowKey: String, columnFamilies: TraversableOnce[String]): Get = {
    val get = new Get(Bytes.toBytes(rowKey))
    columnFamilies.foreach(fam => get.addFamily(Bytes.toBytes(fam)))
    get
  }

  def printVerboseQueryInfo(scan: Scan): Unit = {
    val map = familyMap(scan.getFamilyMap).mkString(",")

    Log.info("Scanning table: " + "bi_user_label")
    Log.info("FamilyMap: " + map)
    Log.info("Start row: " + Bytes.toString(scan.getStartRow))
    Log.info("Stop row: " + Bytes.toString(scan.getStopRow))
    Log.info("Filter: " + scan.getFilter.toString)
  }

  def familyMap(map: java.util.Map[Array[Byte], java.util.NavigableSet[Array[Byte]]]) = {
    map.toList.flatMap {
      case (fam, cols) =>
        val famStr = Bytes.toString(fam)
        if (cols != null) {
          cols map (col => (famStr, Bytes.toString(col)))
        } else {
          List(famStr, "ANY COLUMN")
        }
    }
  }

  def printVerboseQueryInfo(get: Get): Unit = {
    val map = familyMap(get.getFamilyMap).mkString(",")

    Log.info("Getting row from table: " + "bi_user_label")
    Log.info("Row key: " + Bytes.toString(get.getRow))
    Log.info("FamilyMap: " + map)
    Log.info("Filter: " + get.getFilter)
  }

  def get(get: Get): Result = {
    printVerboseQueryInfo(get)
    getHtable.get(get)
  }

  def main(args: Array[String]) {
    val columnFamilies = labelTable.getTableDescriptor.getColumnFamilies.map( p => p.getNameAsString)
    val value = get(getGet("872547", columnFamilies)).getValue(Bytes.toBytes("labels"), Bytes.toBytes(""))
    println(Bytes.toString(value))
    //printVerboseQueryInfo(scan)
  }
}
