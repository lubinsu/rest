package com.changtu.service

import akka.actor.{Actor, ActorLogging}
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.Put
import com.changtu.core.{ResultMsg, _}
import com.changtu.util.hbase.HBaseClient
import com.changtu.util.redis.RedisUtils
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.filter._
import org.apache.hadoop.hbase.util.Bytes
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Extraction, Formats}
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

  // 时间格式隐式转换
  val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val redisClient = new RedisUtils("redis.server.test.1")

  implicit def str2date(str: String): DateTime = DateTime.parse(str, formatter)

  val mediator = DistributedPubSubExtension(context.system).mediator
  /*val cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,

      classOf[MemberUp], classOf[MemberRemoved], classOf[UnreachableMember])

  }*/

  // 创建redis连接
  //val r = new RedisClient("172.19.3.62", 45001)
  //创建HBASE连接
  val labelStr = new HBaseClient(tablePath = "bi_user_label_code_string")

  // 过滤条件
  val labelFilter = new FilterList(FilterList.Operator.MUST_PASS_ALL)
  labelFilter.addFilter(scvf("labels", "", CompareOp.NOT_EQUAL, "13"))
  labelFilter.addFilter(scvf("labels", "", CompareOp.NOT_EQUAL, "14"))

  val blackList = new HBaseClient(tablePath = "afs_member_lists")
  val blackFilter = new FilterList(FilterList.Operator.MUST_PASS_ALL)
  blackFilter.addFilter(scvf("p", "flag", CompareOp.EQUAL, "Y"))


  val abnormalPayIdList = new HBaseClient(tablePath = "bi_feature_library_pay_id")

  // 过滤条件
  val abnormalFilter = new FilterList(FilterList.Operator.MUST_PASS_ALL)
  abnormalFilter.addFilter(scvf("p", "type", CompareOp.NOT_EQUAL, "4"))
  abnormalFilter.addFilter(scvf("p", "type", CompareOp.NOT_EQUAL, "5"))
  abnormalFilter.addFilter(scvf("p", "type", CompareOp.NOT_EQUAL, "7"))
  abnormalFilter.addFilter(scvf("p", "type", CompareOp.NOT_EQUAL, "0"))

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
      * 注册风险控制
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
      val result = orderSubmitValid(orderSubmit) match {
        case Success(returnMsg) =>
          log.info("[orderSubmit] [SUCCESS] ".concat(compact(render(Extraction.decompose(returnMsg)))))
          returnMsg
        case Failure(f) =>
          val returnMsg = ReturnMsg("BIG_DATA_ERR_00004", "orderSubmit validate error.".concat(f.getMessage), ArrayBuffer.empty[ResultMsg])
          log.error("[orderSubmit] [ERROR] ".concat(compact(render(Extraction.decompose(returnMsg)))))
          returnMsg
      }
      sender() ! result

    /**
      * 支付返回风险控制
      */
    case payReturn: PayReturn =>
      val result = payReturnValid(payReturn) match {
        case Success(returnMsg) =>
          log.info("[payReturn] [SUCCESS] ".concat(compact(render(Extraction.decompose(returnMsg)))))
          returnMsg
        case Failure(f) =>
          val returnMsg = ReturnMsg("BIG_DATA_ERR_00005", "payReturn validate error.".concat(f.getMessage), ArrayBuffer.empty[ResultMsg])
          log.error("[payReturn] [ERROR] ".concat(compact(render(Extraction.decompose(returnMsg)))))
          returnMsg
      }
      sender() ! result
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
    * 同程代购	01
    * 携程代购	02
    * 12308代购	03
    * 去哪儿代购	04
    * 未知代购	05
    * 下单数量异常	06
    *
    * @param login 登录参数
    * @return 返回风险评估结果
    */
  private[service] def loginValid(login: Login): Try[ReturnMsg] = {
    Try {
      val rs = ArrayBuffer.empty[ResultMsg]

      // 1.白名单用户ID匹配
      if (!checkWhiteList(login.userId)) {
        // 2.代购用户ID匹配
        val get = labelStr.getGet(login.userId.concat("_").concat("abnormal_order_class")).setFilter(labelFilter)

        if (labelStr.table.exists(get)) {
          val value = labelStr.get(get).getValue(Bytes.toBytes("labels"), Bytes.toBytes(""))
          val labels = Bytes.toString(value)

          labels.split(",").foreach(label => {
            rs += ResultMsg("02", label match {
              case "10" => "03" // 携程
              case "11" => "05" // 去哪儿
              case "12" => "04" // 12308
              case "15" => "02" // 同程网
              case _ => "06"
            })
          })
        } else {
          rs += ResultMsg("01", "01")
        }
      }

      // 返回结果
      returnMsg(rs)
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
      // WHITE_${INFO_TYPE}_${USER_INFO}
      // BLACK_${INFO_TYPE}_${USER_INFO}
      // BLACK_2_13001212547
      // 目前只验证手机号
      val get = blackList.getGet("BLACK_2_".concat(register.smsMobile)).setFilter(blackFilter)
      if (blackList.table.exists(get)) {

        val start = blackList.get(get).getValue(Bytes.toBytes("p"), Bytes.toBytes("st"))
        val end = blackList.get(get).getValue(Bytes.toBytes("p"), Bytes.toBytes("en"))

        // 判断是否在生效时间内
        if (effectTime(Bytes.toString(start), Bytes.toString(end))) rs += ResultMsg("03", "08")

      } else {
        rs += ResultMsg("01", "01")
      }

      // 返回结果
      returnMsg(rs)
    }

  }

  /**
    * 保存订单风险验证
    *
    * 1.白名单用户
    * 2.用户ID是否代购账号
    *
    * @param orderSubmit 保存订单参数
    * @return 返回风险评估结果
    */
  private[service] def orderSubmitValid(orderSubmit: OrderSubmit): Try[ReturnMsg] = {
    Try {
      val rs = ArrayBuffer.empty[ResultMsg]
      //  1.白名单用户
      //  2.用户ID是否代购账号
      if (!checkWhiteList(orderSubmit.userId)) {
        val get = labelStr.getGet(orderSubmit.userId.concat("_").concat("abnormal_order_class")).setFilter(labelFilter)
        if (labelStr.table.exists(get)) {
          val value = labelStr.get(get).getValue(Bytes.toBytes("labels"), Bytes.toBytes(""))
          val labels = Bytes.toString(value)

          labels.split(",").foreach(label => {
            rs += ResultMsg("02", label match {
              case "10" => "03" // 携程
              case "11" => "05" // 去哪儿
              case "12" => "04" // 12308
              case "15" => "02" // 同程网
              case _ => "06"    // 未知代购
            })

          })

        } else {
          rs += ResultMsg("01", "01")
        }
      }
      // 返回结果
      returnMsg(rs)
    }
  }

  /**
    * 支付完成风险验证
    *
    * 1.白名单用户
    * 2.支付账号是否代购账号
    *
    * @param payReturn 支付完成参数
    * @return 返回风险评估结果
    */
  private[service] def payReturnValid(payReturn: PayReturn): Try[ReturnMsg] = {
    Try {
      val rs = ArrayBuffer.empty[ResultMsg]
      //  1.白名单用户
      if (!checkWhiteList(payReturn.userId) & payReturn.buyerEmail.length > 0) {
        //  2.支付账号是否代购账号(添加过滤条件)
        val get = abnormalPayIdList.getGet(payReturn.buyerEmail).setFilter(abnormalFilter)
        if (abnormalPayIdList.table.exists(get)) {

          val value = abnormalPayIdList.get(get).getValue(Bytes.toBytes("p"), Bytes.toBytes("type"))
          val idType = Bytes.toString(value)

          rs += ResultMsg("02", idType match {
            case "1" => "03" // 携程
            case "2" => "05" // 去哪儿
            case "3" => "04" // 12308
            case "6" => "02" // 同程网
            case _ => "06"   // 未知代购
          })

        } else {
          rs += ResultMsg("01", "01")
        }
      }

      // 返回结果
      returnMsg(rs)
    }
  }

  private[service] def checkWhiteList(userId: String): Boolean = {
    // 目前白名单只针对USER_ID
    val info = redisClient.get("WHITE_1_".concat(userId))
    info match {
      case Some(json) =>

        val whiteJson = parse(json)
        val isUserId = (whiteJson \ "infoType").extract[String] == "1"
        val ectFlag = (whiteJson \ "ectFlag").extract[String] == "Y"
        val effected = effectTime((whiteJson \ "stDate").extract[String], (whiteJson \ "enDate").extract[String])

        ectFlag && effected && isUserId
      case None => false
    }
  }

  /**
    * 返回值数据处理
    *
    * @param rs 结果集
    * @return 封装结果返回
    */
  private[service] def returnMsg(rs: ArrayBuffer[ResultMsg]): ReturnMsg = {
    if (rs.isEmpty) {
      rs += ResultMsg("01", "01")
    }
    ReturnMsg("0", "成功返回", rs)
  }

  /**
    * SingleColumnValueFilter
    *
    * @param cf       列族
    * @param c        列名
    * @param operator 操作 [[org.apache.hadoop.hbase.filter.CompareFilter.CompareOp]]
    * @param value    要过滤的值
    * @return
    */
  private[service] def scvf(cf: String, c: String, operator: CompareOp, value: String): SingleColumnValueFilter = {
    new SingleColumnValueFilter(
      Bytes.toBytes(cf),
      Bytes.toBytes(c),
      operator,
      Bytes.toBytes(value)
    )
  }

  private[service] def effectTime(start: String, end: String): Boolean = {
    (DateTime.now().getMillis > start.getMillis) && (DateTime.now().getMillis < end.getMillis)
  }
}
