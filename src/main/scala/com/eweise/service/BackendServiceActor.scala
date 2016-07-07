package com.eweise.service

import akka.actor.{Actor, ActorLogging}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Put
import com.changtu.jsonprotocol.UserLabels
import com.changtu.utils.hbase.HBaseClient
import org.apache.hadoop.hbase.util.Bytes

@SerialVersionUID(1L)
sealed trait Message

case class PerformWork(msg: String) extends Message

case object OK extends Message

class BackendServiceActor extends Actor with ActorLogging {

  val mediator = DistributedPubSubExtension(context.system).mediator

  //创建HBASE连接
  val labelStr = new HBaseClient(tablePath = "bi_user_label_string")
  val labelDtl = new HBaseClient(tablePath = "bi_user_label")
  mediator ! Put(self)

  def receive = {
    case PerformWork(message) =>
      log.info("Backend Service is performing some work")
      sender() ! message
    case userId: String =>
      log.info("Backend Service is querying user's labels:" + userId)
      sender() ! getLabels(userId)
  }

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
}
