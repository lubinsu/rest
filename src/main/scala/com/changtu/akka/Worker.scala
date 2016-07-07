// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/22/2016.
  *
  */

import akka.actor.Actor

class Worker extends Actor {
  def receive = {
    case MessageFind3(message: String) =>
      sender() ! ResultMsg2("Got ".concat(message))
    /*println("Worker:" + message)
    print("[" + message + "] ")*/
  }
}
