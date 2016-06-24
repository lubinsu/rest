// Copyright (C) 2015-2016 the original author or authors.
package com.changtu.akka

/**
  * Created by lubinsu on 6/21/2016.
  * Akka分布式实现
  */

import akka.actor.Actor
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props

case class AkkaMessage(message: Any)

case class Response(response: Any)

/**
  * author www.lubinsu.com
  */
class Server extends Actor {
  override def receive: Receive = {
    //接收到的消息类型为AkkaMessage，则在前面加上response_，返回给Sender
    case msg: AkkaMessage =>
      println("服务端收到消息: " + msg.message)
      sender ! Response("response_" + msg.message)
    case _ => println("服务端不支持的消息类型 .. ")
  }
}

object Server {
  //创建远程Actor:ServerSystem
  def main(args: Array[String]): Unit = {
    val serverSystem = ActorSystem("lubinsu", ConfigFactory.parseString(
      """
      akka {
       actor {
          provider = "akka.remote.RemoteActorRefProvider"
        }
        remote {
          enabled-transports = ["akka.remote.netty.tcp"]
          netty.tcp {
            hostname = "172.18.5.119"
            port = 2600
          }
        }
      }
      """))

    serverSystem.actorOf(Props[Server], "server")
    
  }
}
