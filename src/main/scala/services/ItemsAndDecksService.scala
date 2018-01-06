package services

import actors.ItemsAndDecksActor.CreateDeckRequest
import io.circe._
import io.circe.parser._
import io.circe.Decoder
import models.exceptions.JsonParsingException._
import io.circe.generic.semiauto._
import models.exceptions.DeckParsingException
import models.ids.Ids
import models.ids.Ids.{DeckId, UserId}
import models.itemsAndDecks._
import models.users.UserTable
import slick.jdbc.PostgresProfile
import models.postgresProfile.MyPostgresProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}


class ItemsAndDecksService(implicit db: PostgresProfile.backend.Database, executionContext: ExecutionContext) {

  /**
    * Parses JSON from String
    *
    * @param string JSON String
    * @return Exception if JSON cannot be parsed and Sequence of Either major exceptions or Items with minor exceptions
    */
  def getItemsOutOfString(string: String): Either[JSONParsingException, (Seq[ParsedItem], Seq[ItemParsingException])] = parse(string) match {
    case Left(err) => Left(JSONParsingException(err.message))
    case Right(json) => decodeJson(json) match {
      case Left(err) => Left(JSONParsingException(err.message))
      case Right(data) => Right(validateItems(data))
    }
  }


  def validateItems(items: Seq[ParsedItem]): (Seq[ParsedItem], Seq[ItemParsingException]) = {
    val itemsAndExceptions = items.indices.zip(items).map { case (index, item) =>
      if (item.validate.isEmpty)
        (Some(item), None)
      else item.validate.get match {
        case MajorItemParsingException(message) => (None, Some(MajorItemParsingException(s"$message for item $index.")))
        case MinorItemParsingException(message) => (Some(item), Some(MinorItemParsingException(s"$message for item $index.")))
      }
    }.unzip
    (itemsAndExceptions._1.flatten, itemsAndExceptions._2.flatten)
  }

  private def decodeJson(json: Json): Either[JSONParsingException, Seq[ParsedItem]] = {
    implicit val decodeItem: Decoder[ParsedItem] = deriveDecoder[ParsedItem]
    json.hcursor.as[Seq[ParsedItem]] match {
      case Left(err) => Left(JSONParsingException(err.message))
      case Right(data) => Right(data)
    }
  }

  def createDeck(request: CreateDeckRequest): Future[Either[DeckParsingException, DeckId]] = {
    val newDeckInfo = DecksInfoEntry(name = request.name, authorId = UserId(request.userId.toLong), subscribers = List())
    val tq = TableQuery[Deck]((tag: Tag) => new Deck(tag)(request.name + request.userId))
    val itemsToAdd = request.items.map(itemEntryOutOfParsedItem)
    db.run(tq.schema.create.andThen(tq ++= itemsToAdd).andThen(decks returning decks.map(_.id) += newDeckInfo).map(id => Right(id)))
  }

  def getDecksByUserId(id: UserId): Future[Seq[(DeckId, String)]] =
    db.run(decks.filter(deck => id.value.bind === deck.subscribers.any || deck.authorId.value === id).map(deck => (deck.id, deck.name)).result)


  private def itemEntryOutOfParsedItem(item: ParsedItem): ItemEntry = { //Shapeless?
    val maybeText = item.text.getOrElse("")
    val maybeLevel = item.level.getOrElse(1)
    val maybeMeaningVariants = item.meaningVariants.getOrElse(List())
    val maybeReadingVariants = item.readingVariants.getOrElse(List())
    val maybeDescription = item.description.getOrElse("")
    val maybePrecedence = item.precedence.getOrElse(10)
    ItemEntry(text = "",
      level = maybeLevel,
      meaningVariants = maybeMeaningVariants,
      readingVariants = maybeReadingVariants,
      description = maybeDescription,
      precedence = maybePrecedence,
      unlockDate = None,
      reviewDate = None)
  }

  def checkIfDeckNameIsOccupied(name: String): Future[Boolean] =
    db.run(MTable.getTables(name)).map(maybeTable => maybeTable.isEmpty)


  def getDecks: Future[Seq[(String, String)]] = {
    val users = TableQuery[UserTable]
    val query = for {
      (authorId, deckName) <- decks.map(col => (col.authorId, col.name))
      userName <- users.filter(_.id === authorId).map(_.login)
    }
      yield (deckName, userName)
    db.run(query.result)
  }

  //Maps level and the amount of hours to wait between reviews
  private val reviewDateCalculator = Map(2 -> 8, 3 -> 24, 4 -> 48, 5 -> 7 * 24, 6 -> 14 * 24, 7 -> 30 * 24, 8 -> 120 * 24).withDefaultValue(4)
  //private val items = TableQuery[Deck]
  val decks = TableQuery[DecksInfoTable]
}
