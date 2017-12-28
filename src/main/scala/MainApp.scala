
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import services.HttpService

import scala.concurrent.ExecutionContext


object MainApp extends App {


  implicit val system: ActorSystem = ActorSystem("flashcards")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val executionContext: ExecutionContext = system.dispatcher
  val httpService = new HttpService
  var port = scala.util.Properties.envOrElse("PORT", "8080").toInt
  println(port)
  var http = scala.util.Properties.envOrElse("HOST", "localhost")
  Http().bindAndHandle(httpService.routes, http, port)

}

