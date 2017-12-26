package actors

import akka.actor.Actor
import actors.ReviewItemsActor._
import actors.UserAuthActor._
import services.{AuthService, ItemService}

import scala.concurrent.ExecutionContext

class MainActor(implicit db: slick.jdbc.PostgresProfile.backend.Database, executionContext: ExecutionContext) extends Actor {
  implicit val itemService = new ItemService
  implicit val authService = new AuthService
  val itemsActor = context.actorOf(ReviewItemsActor.props)
  val authActor = context.actorOf(UserAuthActor.props)


  def receive: Receive = {
    case message: ParseItems => itemsActor forward message
    case message: LogInRequest => authActor forward message
    case message: SignUpRequest => authActor forward message
  }
}
