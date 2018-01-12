package actors

import akka.actor.{Actor, ActorRef}
import actors.ItemsAndDecksActor._
import actors.UserAuthActor._
import models.postgresProfile.MyPostgresProfile
import services.{AuthService, DeckService}

import scala.concurrent.ExecutionContext

class MainActor(implicit db: MyPostgresProfile.backend.Database) extends Actor {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val itemService: DeckService = new DeckService
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
    case message: GetItemsByDeckId => itemsAndDecksActor forward message
  }
}
