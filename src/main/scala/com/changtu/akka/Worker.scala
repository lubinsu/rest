// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/22/2016.
  *
  */

import akka.actor.{Actor, ActorRef}

class Worker extends Actor {
  def receive = {
    case WorkMsg(message: String, replyTo: ActorRef) =>
      replyTo ! ResultMsg("Got ".concat(message))
      print("[" + message + "] ")
  }
}
