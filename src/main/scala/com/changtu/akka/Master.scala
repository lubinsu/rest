// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by 6526 on 6/22/2016.
  */

import scala.collection.mutable.ListBuffer
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.routing.FromConfig

sealed class Helper(count: Int, replyTo: ActorRef) extends Actor {
  val perfectNumbers = new ListBuffer[Int]
  var nrOfResult = 0

  def receive = {
    case Result(num: Int, isPerfect: Boolean) =>
      nrOfResult += 1
      if (isPerfect)
        perfectNumbers += num
      if (nrOfResult == count)
        replyTo ! PerfectNumbers(perfectNumbers.toList)
  }
}

class Master extends Actor {
  val worker = context.actorOf(Props[Worker].withRouter(FromConfig()), "workerRouter")

  def receive = {
    case StartFind(start: Int, end: Int, replyTo: ActorRef) if start > 1 && end >= start =>
      val count = end - start + 1
      val helper = context.actorOf(Props(new Helper(count, replyTo)))
//      (start to end).foreach(num => worker ! Work(num, helper))
      (start to end).foreach(num => worker.tell(ConsistentHashableEnvelope(Work(num,helper), num), helper))
  }
}
