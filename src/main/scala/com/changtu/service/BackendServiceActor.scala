package com.changtu.service

import akka.actor.{Actor, ActorLogging}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Put
import com.changtu.core.{ResultMsg, _}
import com.changtu.util.hbase.HBaseClient
import org.apache.hadoop.hbase.util.Bytes
import org.json4s.{DefaultFormats, Extraction, Formats}
import org.json4s.jackson.JsonMethods._
import spray.httpx.Json4sSupport

import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}


@SerialVersionUID(1L)
sealed trait Message

case class BusScenic(userId: String, labelCode: String) extends Message

object JsonProtocol extends Json4sSupport {
  //  implicit def fooFormat: Formats = DefaultFormats
  implicit def json4sFormats: Formats = DefaultFormats
}
/**
  * 微服务后台接口
  * 1.车景套餐相关
  * 2.风控相关（登录、注册、保存订单、支付）
  */
class BackendServiceActor extends Actor with ActorLogging {

  import JsonProtocol._
  val mediator = DistributedPubSubExtension(context.system).mediator
  //创建HBASE连接
  val labelStr = new HBaseClient(tablePath = "bi_user_label_code_string")
  val blackList = new HBaseClient(tablePath = "bi_ods_coa_mbl_exce_mgr")
  mediator ! Put(self)

  /**
    * 注意记录日志
    *
    * @return
    */
  def receive = {
    /**
      * 车景套餐接口
      */
    case BusScenic(userId, labelCode) =>
//      val result = getLabels(BusScenic(userId, labelCode)).getOrElse(UserLabels(userId, "", -1))
      val result = getLabels(BusScenic(userId, labelCode)) match {
        case Success(userLabels) =>
          log.info("[userLabels] [SUCCESS] ".concat(compact(render(Extraction.decompose(userLabels)))))
          userLabels
        case Failure(f) =>
          val userLabels = UserLabels(userId, "", -1)
          log.error("[userLabels] [ERROR] ".concat(f.getMessage).concat(compact(render(Extraction.decompose(userLabels)))))
          userLabels
      }
      sender() ! result

    /**
      * 登录风险控制
      */
    case login: Login =>
      val result = loginValid(login) match {
        case Success(returnMsg) =>
          log.info("[login] [SUCCESS] ".concat(compact(render(Extraction.decompose(returnMsg)))))
          returnMsg
        case Failure(f) =>
          val returnMsg = ReturnMsg("BIG_DATA_ERR_00002", "login validate error.".concat(f.getMessage), ArrayBuffer.empty[ResultMsg])
          log.error("[login] [ERROR] ".concat(compact(render(Extraction.decompose(returnMsg)))))
          returnMsg
      }
      sender() ! result

    /**
      * 注册风险该控制
      */
    case register: Register =>
      val result = registerValid(register) match {
        case Success(returnMsg) =>
          log.info("[register] [SUCCESS] ".concat(compact(render(Extraction.decompose(returnMsg)))))
          returnMsg
        case Failure(f) =>
          val returnMsg = ReturnMsg("BIG_DATA_ERR_00003", "register validate error.".concat(f.getMessage), ArrayBuffer.empty[ResultMsg])
          log.error("[register] [ERROR] ".concat(compact(render(Extraction.decompose(returnMsg)))))
          returnMsg
      }
      sender() ! result

    /**
      * 保存订单风险控制
      */
    case orderSubmit: OrderSubmit =>
      sender() ! orderSubmitValid(orderSubmit)

    /**
      * 支付返回风险控制
      */
    case payReturn: PayReturn =>
      sender() ! payReturnValid(payReturn)
  }

  /**
    * 获取某个用户的标签列表
    *
    * @param busScenic 车景信息
    * @return 返回车景套餐的标签
    */
  private[service] def getLabels(busScenic: BusScenic): Try[UserLabels] = {
    Try {
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

  /**
    * 登录风险验证
    *
    * 1.白名单用户ID匹配
    *
    * 2.代购用户ID匹配
    *   同程代购	01
    *   携程代购	02
    *   12308代购	03
    *   去哪儿代购	04
    *   未知代购	05
    *   下单数量异常	06
    *
    * @param login 登录参数
    * @return 返回风险评估结果
    */
  private[service] def loginValid(login: Login): Try[ReturnMsg] = {
    Try {
      val rs = ArrayBuffer.empty[ResultMsg]

      // 1.白名单用户ID匹配
      // TODO
      checkWhiteList(login.userId)
      // 2.代购用户ID匹配
      val get = labelStr.getGet(login.userId.concat("_").concat("abnormal_order_class"))

      if (labelStr.table.exists(get)) {
        val value = labelStr.get(get).getValue(Bytes.toBytes("labels"), Bytes.toBytes(""))
        val labels = Bytes.toString(value)

        labels.split(",").foreach( label => {
          rs += ResultMsg("02", label match {
            case "10" => "02" // 携程
            case "11" => "04" // 去哪儿
            case "12" => "03" // 12308
            case "13" => "05" // 逍遥行商旅网
            case "14" => "05" // 我要买票网
            case "15" => "01" // 同程网
          })

        })

      } else {
        rs += ResultMsg("01", "01")
      }

      ReturnMsg("0", "成功返回", rs)
    }

  }

  /**
    * 注册风险验证
    * 1.经过安全小组抓取的打码平台黑名单手机号数据不予以注册
    *
    * @param register 注册参数
    * @return 返回风险评估结果
    */
  private[service] def registerValid(register: Register): Try[ReturnMsg] = {

    Try {
      val rs = ArrayBuffer.empty[ResultMsg]
      val get = blackList.getGet(register.smsMobile)
      if (blackList.table.exists(get)) {
        rs += ResultMsg("03", "01")
      } else {
        rs += ResultMsg("01", "01")
      }

      ReturnMsg("0", "成功返回", rs)
    }

  }

  /**
    * 保存订单风险验证
    *
    * @param orderSubmit 保存订单参数
    * @return 返回风险评估结果
    */
  private[service] def orderSubmitValid(orderSubmit: OrderSubmit): Try[ReturnMsg] = {
    Try {
      val rs = ArrayBuffer.empty[ResultMsg]
      rs += ResultMsg("riskClass_".concat(orderSubmit.userId), "riskLevelId_1".concat(orderSubmit.userId))
      ReturnMsg("0", "测试用例", rs)
    }
  }

  /**
    * 支付完成风险验证
    *
    * @param payReturn 支付完成参数
    * @return 返回风险评估结果
    */
  private[service] def payReturnValid(payReturn: PayReturn): Try[ReturnMsg] = {
    Try {
      val rs = ArrayBuffer.empty[ResultMsg]
      rs += ResultMsg("riskClass_".concat(payReturn.orderId), "riskLevelId_1".concat(payReturn.orderId))
      ReturnMsg("0", "测试用例", rs)
    }
  }

  private[service] def checkWhiteList(userId: String): Boolean = {
    // TODO
    true
  }
}
