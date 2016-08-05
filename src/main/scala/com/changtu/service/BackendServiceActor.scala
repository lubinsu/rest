package com.changtu.service

import akka.actor.{Actor, ActorLogging}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Put
import com.changtu.core.UserLabels
import com.changtu.util.hbase.HBaseClient
import org.apache.hadoop.hbase.util.Bytes

@SerialVersionUID(1L)
sealed trait Message

case class BusScenic(userId: String, labelCode: String) extends Message

class BackendServiceActor extends Actor with ActorLogging {

  val mediator = DistributedPubSubExtension(context.system).mediator

  //创建HBASE连接
  val labelStr = new HBaseClient(tablePath = "bi_user_label_code_string")
  mediator ! Put(self)

  def receive = {
    case BusScenic(userId, labelCode) =>
      log.info("Backend Service is querying user's labels:" + userId)
      sender() ! getLabels(BusScenic(userId, labelCode))
  }

  // 获取某个用户的标签列表
  def getLabels(busScenic: BusScenic): UserLabels = {
    val get = labelStr.getGet(busScenic.userId.concat("_").concat(busScenic.labelCode))

    if (labelStr.table.exists(get)) {
      val value = labelStr.get(get).getValue(Bytes.toBytes("labels"), Bytes.toBytes(""))
      val labels = Bytes.toString(value)
      UserLabels(busScenic.userId, labels, 0)
    } else {
      UserLabels(busScenic.userId, "", -1)
    }
  }
}
