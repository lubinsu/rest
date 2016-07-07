package com.changtu.spray

/**
  * Created by lubinsu on 7/5/2016.
  */

import akka.actor.{Actor, ActorLogging, Address, RelativeActorPath, RootActorPath}
import akka.cluster.{Cluster, ClusterEvent}

import scala.concurrent.forkjoin.ThreadLocalRandom

class MainCluster extends Actor with ActorLogging {
  val cluster = Cluster(context.system)

  cluster.subscribe(self, classOf[ClusterEvent.MemberUp])
  cluster.subscribe(self, classOf[ClusterEvent.MemberRemoved])

  cluster.join(cluster.selfAddress)
  var nodes = Set.empty[Address]

  val servicePath = "/user/agent"
  val servicePathElements = servicePath match {
    case RelativeActorPath(elements) => elements
    case _ => throw new IllegalArgumentException(
      "servicePath [%s] is not a valid relative actor path" format servicePath)
  }

  override def receive: Receive = {

    case ClusterEvent.MemberUp(member) =>
      if (member.address != cluster.selfAddress) {
        nodes += member.address
      }

    case ClusterEvent.MemberRemoved(member, _) =>
      nodes -= member.address
    case MessageFind(message) =>
      println("node size:" + nodes.size)
      val address = nodes.toIndexedSeq(ThreadLocalRandom.current.nextInt(nodes.size))
      val service = context.actorSelection(RootActorPath(address) / servicePathElements)
      service ! MessageFind(message)
      println("send to :" + address)

  }
}
