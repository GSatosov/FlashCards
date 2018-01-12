package services

import java.sql.Timestamp

import actors.ItemsAndDecksActor.CreateDeckRequest
import models.exceptions.{DeckParsingException, UrlAccessException}
import models.ids.Ids.{DeckId, UserId}
import models.itemsAndDecks._
import models.postgresProfile.MyPostgresProfile
import models.users.UserTable

import models.postgresProfile.MyPostgresProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}


class DeckService(implicit db: MyPostgresProfile.backend.Database, executionContext: ExecutionContext) {
  createSchemaIfNotExists
  val decks = TableQuery[DecksInfoTable]

  //Maps level and the amount of hours to wait between reviews
  private val reviewDateCalculator = Map(2 -> 8, 3 -> 24, 4 -> 48, 5 -> 7 * 24, 6 -> 14 * 24, 7 -> 30 * 24, 8 -> 120 * 24).withDefaultValue(4)

  private def createSchemaIfNotExists: Unit =
    db.run(MTable.getTables("decks")).foreach(tables => if (tables.isEmpty) db.run(decks.schema.create))

  def createDeck(request: CreateDeckRequest): Future[Right[DeckParsingException, DeckId]] = {
    val newDeckInfo = DecksInfoEntry(name = request.name, authorId = UserId(request.userId.toLong), basedOn = None)
    val tq = TableQuery[Deck]((tag: Tag) => new Deck(tag)(request.name + "_" + request.userId))
    val itemsToAdd = request.items.map(itemEntryOutOfParsedItem)
    db.run(tq.schema.create.andThen(tq ++= itemsToAdd).andThen(decks returning decks.map(_.id) += newDeckInfo).map(id => Right(id)))
  }

  def getDecksByUserId(id: UserId): Future[Seq[(DeckId, String)]] =
    db.run(decks.filter(_.authorId.value === id).map(deck => (deck.id, deck.name)).result)

  private def itemEntryOutOfParsedItem(item: ParsedItem): ItemEntry = { //Shapeless?
    val maybeText = item.text.getOrElse("")
    val maybeLevel = item.level.getOrElse(1)
    val maybeMeaningVariants = item.meaningVariants.getOrElse(List())
    val maybeReadingVariants = item.readingVariants.getOrElse(List())
    val maybeDescription = item.description.getOrElse("")
    val maybePrecedence = item.precedence.getOrElse(10)
    val maybeUnlockDate, maybeReviewDate = if (maybeLevel == 1) Some(new Timestamp(System.currentTimeMillis())) else None
    ItemEntry(text = maybeText,
      level = maybeLevel,
      meaningVariants = maybeMeaningVariants,
      readingVariants = maybeReadingVariants,
      description = maybeDescription,
      precedence = maybePrecedence,
      unlockDate = maybeUnlockDate,
      reviewDate = maybeReviewDate)
  }

  def checkIfDeckNameIsOccupied(name: String): Future[Boolean] =
    db.run(MTable.getTables(name)).map(maybeTable => maybeTable.isEmpty)

  def getItemsByDeckId(deckId: DeckId): Future[Either[UrlAccessException, Seq[ItemEntry]]] = {
    db.run(decks.filter(_.id === deckId).map(_.name).result).flatMap {
      case Nil => Future.successful(Left(UrlAccessException("No such deck present.")))
      case x :: _ => constructTableQueryAndReturnItems(x, deckId)
    }
  }

  private def constructTableQueryAndReturnItems(boardName: String, deckId: DeckId): Future[Right[UrlAccessException, Seq[ItemEntry]]] = {
    val tq = TableQuery[Deck]((tag: Tag) => new Deck(tag)(boardName + "_" + deckId.value))
    db.run(tq.result).map(Right(_))
  }

  def getDecks: Future[Seq[(String, String)]] = {
    val users = TableQuery[UserTable]
    val query = for {
      (authorId, deckName) <- decks.map(col => (col.authorId, col.name))
      userName <- users.filter(_.id === authorId).map(_.login)
    }
      yield (deckName, userName)
    db.run(query.result)
  }



}
