package routes

import actors.ReviewItemsActor.ParseItems
import akka.actor.ActorRef

import akka.pattern.ask

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout

import models.exceptions.JsonParsingException.JSONParsingException
import models.items.ParsedItem

import scala.util.{Failure, Success}


class ItemsRoute(implicit mainActor: ActorRef) {

  implicit lazy val timeout = Timeout(5 seconds)


  lazy val routes: Route =
  pathPrefix("items") {
    //#users-get-delete
    pathEnd {
      concat(
        //   get {
        //   val items: Future[Seq[ItemEntry]] =
        //     (reviewItemsActor ? GetItems).mapTo[Seq[ParsedItem]]
        //     complete(items.toString)
        //   },
        post {
          entity(as[String]) { itemsToBeParsed =>
            val response = (mainActor ? ParseItems(itemsToBeParsed)).mapTo[Either[JSONParsingException, Seq[ParsedItem]]]
            onComplete(response) {
              case Failure(err) => complete(err.getMessage)
              case Success(value) => value match {
                case Left(err) => complete(err.message)
                case Right(data) => complete(data.toString)
              }
            }
          }
        }
      )
    }
  }
}