// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/22/2016.
  *
  */

import akka.actor.Actor
import akka.actor.ActorRef

class Worker extends Actor {
  private def sumOfFactors(number: Int) = {
    (1 /: (2 until number)) { (sum, i) => if (number % i == 0) sum + i else sum }
  }

  private def isPerfect(num: Int): Boolean = {
    num == sumOfFactors(num)
  }

  def receive = {
    case Work(num: Int, replyTo: ActorRef) =>
      replyTo ! Result(num, isPerfect(num))
      print("[" + num + "] ")
  }
}
