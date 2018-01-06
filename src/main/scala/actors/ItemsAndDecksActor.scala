package actors

import actors.ItemsAndDecksActor._
import akka.actor.{Actor, Props}
import models.exceptions.DeckParsingException
import models.ids.Ids
import models.ids.Ids.UserId
import models.itemsAndDecks.ParsedItem
import services.ItemsAndDecksService

import scala.concurrent.{ExecutionContext, Future}


class ItemsAndDecksActor(implicit itemService: ItemsAndDecksService, executionContext: ExecutionContext) extends Actor {


  override def receive: Receive = {
    case request: CreateDeckRequest =>
      val maybeError = validateCreateDeckRequest(request)
      val response: Future[Either[DeckParsingException, Ids.DeckId]] = maybeError.flatMap {
        case None => itemService.createDeck(request)
        case Some(error) => Future.successful(Left(error))
      }
      sender() ! response
    case GetDecks =>
      sender() ! itemService.getDecks
    case ParseItems(maybeItems) =>
      sender() ! itemService.getItemsOutOfString(maybeItems)
    case GetDecksByUserId(id) =>
      sender() ! itemService.getDecksByUserId(id)
  }

  def validateCreateDeckRequest(request: CreateDeckRequest): Future[Option[DeckParsingException]] = {
    request.name match {
      case "" => Future.successful(Some(DeckParsingException("Deck name must not be empty.")))
      case name => itemService.checkIfDeckNameIsOccupied(name).map {
        case false => None
        case true => Some(DeckParsingException("This name is already occupied."))
      }
    }
  }

}

object ItemsAndDecksActor {
  def props(implicit itemService: ItemsAndDecksService, executionContext: ExecutionContext) = Props(new ItemsAndDecksActor)

  case class CreateDeckRequest(name: String, items: Seq[ParsedItem], userId: String)

  case class GetDecks()

  case class ParseItems(maybeItems: String)

  case class GetDecksByUserId(userId: UserId)

}