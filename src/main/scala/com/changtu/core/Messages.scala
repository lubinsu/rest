package com.changtu.core

import scala.collection.mutable.ArrayBuffer

/**
  * Created by lubinsu on 9/1/2016.
  * messages for actor
  */


// 请求参数
case class PublicParam(actionId: String, sercetId: String, signature: String, clientIp: String, sourceLoc: String)

case class Register(publicParam: PublicParam, regSource: String, smsMobile: String)

case class Login(publicParam: PublicParam, loginSource: String, smsMobile: String, userId: String)

case class OrderSubmit(publicParam: PublicParam, userId: String, channelId: String, startDate: String, startCityId: String, endCityId: String, contact: String)

case class PayReturn(publicParam: PublicParam, userId: String, orderId: String, startCityId: String, payStatus: String, buyerEmail: String)

// 返回值
case class ResultMsg(riskClass: String, riskLevelId: String)
case class ReturnMsg(status: String, comment: String, resultSet: ArrayBuffer[ResultMsg])

case class ErrorMsg(errCode: Long, msg: String)