import actors.{MainActor, ReviewItemsActor}
import akka.http.scaladsl.Http
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import routes.ItemsRoute
import services.HttpService

import scala.concurrent.ExecutionContext


object MainApp extends App {


  implicit val system: ActorSystem = ActorSystem("flashcards")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val executionContext: ExecutionContext = system.dispatcher
  val httpService = new HttpService

  Http().bindAndHandle(httpService.routes, "localhost", 8080)

}

