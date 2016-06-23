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

  val servicePath = "/user/master"
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
      nodes += member.address
    case MemberRemoved(member, _) =>
      nodes -= member.address
    case _: MemberEvent => // ignore
    case PerfectNumbers(list: List[Int]) =>
      println("\nFound Perfect Numbers:" + list.mkString(","))
      cluster.down(self.path.address)
      context.system.shutdown()
    case StartFind(start: Int, end: Int, resultTo: ActorRef) =>
      println("node size:" + nodes.size)
      nodes.size match {
        case x: Int if x < 1 =>
          Thread.sleep(1000)
          self ! StartFind(start, end, resultTo)
        case _ =>
          val address = nodes.toIndexedSeq(ThreadLocalRandom.current.nextInt(nodes.size))
          val service = context.actorSelection(RootActorPath(address) / servicePathElements)
          service ! StartFind(start, end, resultTo)
          println("send to :" + address)
      }
  }
}
