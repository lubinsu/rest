package com.changtu.dao

/**
  * Created by lubinsu on 9/8/2016.
  */

import com.changtu.util.Logging
import com.changtu.util.hbase.HBaseClient
import com.redis._
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.filter.{FilterList, SingleColumnValueFilter}
import org.apache.hadoop.hbase.util.Bytes
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object RedisExamples extends App with Logging {

  val r = new RedisClient("172.19.3.62", 45001)
  // 时间格式隐式转换
  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  implicit def str2date(str: String): DateTime = DateTime.parse(str, formatter)

  r.set("WHITE_1_8755362", "{\"infoType\":\"1\",\"userInfo\":\"8755362\",\"stDate\":\"2016-09-08 14:30:30\",\"enDate\":\"2016-10-08 14:31:30\",\"ectFlag\":\"Y\"}")

  logger.info(r.get("WHITE_1_8755362").getOrElse("None"))

  /*val blackList = new HBaseClient(tablePath = "afs_member_lists")
  val blackFilter = new FilterList(FilterList.Operator.MUST_PASS_ALL)
    blackFilter.addFilter(scvf("p", "flag", CompareOp.EQUAL, "Y"))

  val get = blackList.getGet("BLACK_2_13001212547").setFilter(blackFilter)
  if (blackList.table.exists(get)) {

    val start = blackList.get(get).getValue(Bytes.toBytes("p"), Bytes.toBytes("st"))
    val end = blackList.get(get).getValue(Bytes.toBytes("p"), Bytes.toBytes("en"))

    // 判断是否在生效时间内
    if (effectTime(Bytes.toString(start), Bytes.toString(end))) println("true")
  }

  private[dao] def effectTime(start: String, end: String): Boolean = {
    (DateTime.now().getMillis > start.getMillis) && (DateTime.now().getMillis < end.getMillis)
  }

  private[dao] def scvf(cf: String, c: String, operator: CompareOp, value: String): SingleColumnValueFilter = {
    new SingleColumnValueFilter(
      Bytes.toBytes(cf),
      Bytes.toBytes(c),
      operator,
      Bytes.toBytes(value)
    )
  }*/
}
