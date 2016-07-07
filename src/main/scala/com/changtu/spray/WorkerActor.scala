// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.spray

/**
  * Created by lubinsu on 6/22/2016.
  *
  */

import akka.actor.{Actor, Props}


object WorkerActor {

  def props(counter: Int): Props = Props(new WorkerActor)

}

class WorkerActor extends Actor with akka.actor.ActorLogging {

  def receive = {
    case message: String =>
      sender ! message + "got something"
  }

}