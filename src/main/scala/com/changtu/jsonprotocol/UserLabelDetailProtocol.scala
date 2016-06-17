package com.changtu.jsonprotocol

import spray.json.DefaultJsonProtocol

/**
  * Created by lubinsu on 2016/6/3.
  * 用户标签详细信息，生成JSON串的协议接口
  */
object UserLabelDetailProtocol extends DefaultJsonProtocol {
  implicit val detailFormat = jsonFormat6(UserLabelDetail)
}

case class UserLabelDetail(userLabelId: String
                           , labelCode: String, createDt: String
                           , modifyDt: String, status: String
                           , errorCode: Int)
