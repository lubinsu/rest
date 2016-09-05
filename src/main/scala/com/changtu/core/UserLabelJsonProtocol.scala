package com.changtu.core

import spray.json.DefaultJsonProtocol

/**
  * Created by lubinsu on 2016/6/3.
  * 用户标签详细信息，生成JSON串的协议接口
  */

object UserLabelJsonProtocol extends DefaultJsonProtocol {
  implicit val labelFormat = jsonFormat3(UserLabels)
}
case class UserLabels(userId: String, labels: String, errorCode: Int)