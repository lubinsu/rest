// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/22/2016.
  */

import akka.actor.ActorRef

sealed trait Message

case class StartFind(start: Int, end: Int, replyTo: ActorRef) extends Message

case class Work(num: Int, replyTo: ActorRef) extends Message

case class WorkMsg(message: String, replyTo: ActorRef) extends Message

case class Result(num: Int, isPerfect: Boolean) extends Message

case class ResultMsg(msg: String, replyTo: ActorRef) extends Message

case class PerfectNumbers(list: List[Int]) extends Message

case class MessageFind(message: String, replyTo: ActorRef) extends Message
