package com.changtu.akka

/**
  * Created by lubinsu on 7/4/2016.
  */


import org.scalatest._
import spray.testkit.ScalatestRouteTest
//import spray.http.HttpEntity
import spray.http.{ContentTypes, HttpEntity}
import spray.http.StatusCodes._

class PerfectSpec extends FreeSpec with Matchers with ScalatestRouteTest with PathSender {

  def actorRefFactory = system

  "The spraysample Route" - {
    "when listing entities" - {
      "returns a JSON list" in {
        //Mix in Json4s, but only for this test

        Get("/userlabel/123") ~> route0 ~> check {
          assert(contentType.mediaType.isApplication)

          //Check content type
          contentType.toString should include("application/json")
          //Try serializaing as a List of Foo
          val response = responseAs[String]
          println(response)
          //response.size should equal(2)
          //response.head.bar should equal("foo1")

          //Check http status
          status should equal(OK)
        }
      }
    }
    "when posting an entity" - {
      "gives a JSON response" in {
        Post("/userlabel/123", HttpEntity(ContentTypes.`application/json`, """{"bar": "woot!"}""")) ~> route0 ~> check {
          val response = responseAs[String]
          println(response)
          //response should include("I got a response")
          status should equal(Created)
        }
      }
    }
  }
}
