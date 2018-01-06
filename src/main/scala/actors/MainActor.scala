package actors

import akka.actor.{Actor, ActorRef}
import actors.ItemsAndDecksActor.{CreateDeckRequest, GetDecks, GetDecksByUserId, ParseItems}
import actors.UserAuthActor._
import services.{AuthService, ItemsAndDecksService}

import scala.concurrent.ExecutionContext

class MainActor(implicit db: slick.jdbc.PostgresProfile.backend.Database, executionContext: ExecutionContext) extends Actor {
  implicit val itemService: ItemsAndDecksService = new ItemsAndDecksService
  implicit val authService: AuthService = new AuthService
  val itemsAndDecksActor: ActorRef = context.actorOf(ItemsAndDecksActor.props)
  val authActor: ActorRef = context.actorOf(UserAuthActor.props)


  def receive: Receive = {
    case message: LogInRequest => authActor forward message
    case message: SignUpRequest => authActor forward message
    case message: CreateDeckRequest => itemsAndDecksActor forward message
    case message: GetDecks => itemsAndDecksActor forward message
    case message: ParseItems => itemsAndDecksActor forward message
    case message: GetDecksByUserId => itemsAndDecksActor forward message
  }
}
