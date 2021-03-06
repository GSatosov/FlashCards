package services

import actors.MainActor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.Route
import routes.{AuthRoute, DeckRoute}
import slick.jdbc.PostgresProfile
import models.postgresProfile.MyPostgresProfile.api._
import akka.http.scaladsl.server.Directives._
import models.postgresProfile.MyPostgresProfile

import scala.concurrent.ExecutionContext

class HttpService(implicit system: ActorSystem, executionContext: ExecutionContext) {
  implicit val db: MyPostgresProfile.backend.Database = Database.forConfig("databaseUrl")
  implicit val mainActor: ActorRef = system.actorOf(Props(new MainActor))
  private val deckRoute = new DeckRoute
  private val authRoute = new AuthRoute
  val routes: Route = authRoute.routes ~ deckRoute.routes
}
