package com.changtu.dao

/**
  * Created by lubinsu on 9/8/2016.
  * 数据库连接示例，未测试成功
  */

import com.changtu.util.Logging
/*import com.typesafe.slick.driver.oracle.OracleDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global*/
import scala.util.{Failure, Success}

object DBExamples extends App with Logging {


  /*class LogOdsInfo(tag: Tag) extends Table[(String, String, Int, Int)](tag, "LOG_ODS_INFO_SLICK") {
    def objName = column[String]("OBJ_NAME", O.PrimaryKey)

    def errInfo = column[String]("ERR_INFO")

    //def operDate = column[Date]("OPER_DATE")

    def operPos = column[Int]("OPER_POS")

    def operResult = column[Int]("OPER_RESULT")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (objName, errInfo, operPos, operResult)
  }

  val logOdsInfo = TableQuery[LogOdsInfo]
  val db = Database.forConfig("orcl")
  /*val db = Database.forURL(
    url = "jdbc:oracle:thin:@172.19.0.94:1521:orcl"
    // url = "jdbc:oracle:thin:@172.19.0.94:1521:orcl"
    , user = "tts_ods"
    , password = "tts_ods"
    , driver = "com.typesafe.slick.driver.oracle.OracleDriver")*/

  val numOrders = db.run(logOdsInfo.length.result)
  numOrders.onComplete {
    case Success(n) => logger.info(s"Number of orders: $n")
    case Failure(e) => logger.error(e.getMessage)
  }*/
  //db.close
  /*val setup = DBIO.seq(
    // Create the tables, including primary and foreign keys
    logOdsInfo.schema.create,

    // Insert some logOdsInfo
    logOdsInfo +=("sp_ods_ecm_order_details", "ORA-30926: unable to get a stable set of rows in the source tables", 3, 0)
  )

  val setupFuture = db.run(setup)


  try {
    println("LogOdsInfo:")
    db.run(logOdsInfo.result).map(_.foreach {
      case (objName, errInfo, operPos, operResult) =>
        println("  " + objName + "\t" + errInfo + "\t" + operPos + "\t" + operResult)
    })
  } finally db.close*/

}
