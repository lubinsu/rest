package com.changtu.akka

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import org.json4s.{DefaultFormats, Formats}
import spray.can.Http
import spray.can.server.Stats
import spray.http.StatusCodes._
import spray.httpx.Json4sSupport
import spray.routing._

import scala.concurrent.duration.DurationInt

//import scala.concurrent.ExecutionContext.Implicits.global

object FooProtocol extends Json4sSupport {
  //  implicit def fooFormat: Formats = DefaultFormats
  implicit def json4sFormats: Formats = DefaultFormats
}

case class Foo(bar: String)

/**
  * Created by 6526 on 6/24/2016.
  */
trait PathSender extends HttpService {

  import FooProtocol._

  //val confHome = if (System.getenv("CONF_HOME") == "") "/appl/conf" else System.getenv("CONF_HOME")
  //val system = context.system
  //val system = ActorSystem("MasterApp", ConfigFactory.parseFile(new File(confHome + "/application.conf")).getConfig("MainDispatch"))

  //def actorRefFactory = context
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(5 seconds)

  val clusterClient = actorRefFactory.actorOf(Props[ClusterClient], "remoteSender")

  val spraysampleRoute = {
    path("entity") {
      get {
        complete(List(Foo("foo1"), Foo("foo2")))
      } ~
        post {
          respondWithStatus(Created) {
            entity(as[Foo]) { someObject =>
              doCreate(someObject)
            }
          }
        }
    } ~
      path ("entity" / Segment) { id =>
        get {
          complete(s"""detail $id""")
        } ~
          post {
            complete(s"""update $id""")
          }
      } ~
      path("stats") {
        complete {
          //This is another way to use the Akka ask pattern
          //with Spray.
          actorRefFactory.actorSelection("/user/IO-HTTP/listener-0")
            .ask(Http.GetStats)(1.second)
            .mapTo[Stats]
        }
      }
  }
  def doCreate[T](foo: Foo) = {
    complete {
      //We use the Ask pattern to return
      //a future from our worker Actor,
      //which then gets passed to the complete
      //directive to finish the request.
      (clusterClient ? MessageFind3(foo.bar))
        .mapTo[ResultMsg2]
        /*.map[String](result => s"I got a response: ${result.msg}")
        .recover { case _ => s"error: ${foo.bar}" }*/
    }
  }

  val route0 = path("userlabel" / Segment) {
    message => get {
      complete {
        List(Foo(message), Foo("foo2"))
      }
    } ~
      post {
        respondWithStatus(Created) {
          entity(as[Foo]) { someObject =>
            doCreate(someObject)
          }
        }
      }
  }

}
