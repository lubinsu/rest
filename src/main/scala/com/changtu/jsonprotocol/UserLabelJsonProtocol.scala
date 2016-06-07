// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.changtu.jsonprotocol

/**
  * Created by lubinsu on 2016/6/2.
  * 用户标签信息，生成JSON串的协议接口
  */

import spray.json.DefaultJsonProtocol

object UserLabelJsonProtocol extends DefaultJsonProtocol {
  implicit val labelFormat = jsonFormat3(UserLabels)
}
case class UserLabels(userId: String, labels: String, errorCode: Int)