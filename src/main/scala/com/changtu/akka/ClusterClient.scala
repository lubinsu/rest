package com.changtu.akka

/**
  * Created by lubinsu on 6/23/2016.
  */

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSelection.toScala
import akka.actor.Address
import akka.actor.RelativeActorPath
import akka.actor.RootActorPath
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.ClusterEvent.MemberRemoved
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.MemberStatus

import scala.concurrent.forkjoin.ThreadLocalRandom

class ClusterClient extends Actor {
  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberEvent])

  override def postStop(): Unit = cluster unsubscribe self

  var nodes = Set.empty[Address]

  val servicePath = "/user/agent"
  val servicePathElements = servicePath match {
    case RelativeActorPath(elements) => elements
    case _ => throw new IllegalArgumentException(
      "servicePath [%s] is not a valid relative actor path" format servicePath)
  }

  def receive = {
    case state: CurrentClusterState =>
      nodes = state.members.collect {
        case m if m.status == MemberStatus.Up => m.address
      }
    case MemberUp(member) =>
      if (member.address.port.get != 2555 && member.address.port.get != 2554) {
        nodes += member.address
      }
    case MemberRemoved(member, _) =>
      nodes -= member.address
    case _: MemberEvent => // ignore

    case MessageFind(message: String, resultTo: ActorRef) =>
      println("node size:" + nodes.size)
      nodes.size match {
        case x: Int if x < 1 =>
          Thread.sleep(500)
          self ! MessageFind(message, resultTo)
        case _ =>
          val address = nodes.toIndexedSeq(ThreadLocalRandom.current.nextInt(nodes.size))
          val service = context.actorSelection(RootActorPath(address) / servicePathElements)
          service ! MessageFind2(message, resultTo, self)
          println("PathSender:" + resultTo)
          println("send to :" + address)
      }
    case ResultMsg(msg: String, replyTo: ActorRef) =>
      println("ClusterClientResult:" + msg)
      println("ClusterClientSender:" + replyTo)
      replyTo ! ResultMsg(msg: String, replyTo: ActorRef)
      println("Sent success")
      /*cluster.down(self.path.address)
      context.system.shutdown()*/
  }
}
