package com.changtu.rest

/**
  * Created by lubinsu on 2016/6/2.
  * 用户标签信息，生成JSON串的协议接口
  */

import spray.json.DefaultJsonProtocol

object UserLabelJsonProtocol extends DefaultJsonProtocol {
  implicit val labelFormat = jsonFormat3(UserLabels)
}
case class UserLabels(userId: String, labels: String, errorCode: Int)