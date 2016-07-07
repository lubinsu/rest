// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.spray

/**
  * Created by lubinsu on 6/22/2016.
  */

sealed trait Message

case class ResultMsg(msg: String) extends Message

case class MessageFind(message: String) extends Message
