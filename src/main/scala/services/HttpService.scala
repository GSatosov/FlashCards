package services

import actors.MainActor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.Route
import routes.{AuthRoute, ItemsRoute}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

class HttpService(implicit system: ActorSystem, executionContext: ExecutionContext) {
  implicit val db: PostgresProfile.backend.Database = Database.forConfig("databaseUrl")
  implicit val mainActor: ActorRef = system.actorOf(Props(new MainActor))
  private val itemsRoute = new ItemsRoute
  private val authRoute = new AuthRoute
  val routes: Route = authRoute.routes ~ itemsRoute.routes
}
