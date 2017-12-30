package services

import io.circe._
import io.circe.parser._
import io.circe.syntax._
import io.circe.Decoder
import io.circe.Encoder
import models.exceptions.JsonParsingException._
import io.circe.generic.semiauto._
import models.items.{Deck, ItemEntry, ParsedItem}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}


class ItemService(implicit db: PostgresProfile.backend.Database, executionContext: ExecutionContext) {

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

  //Maps level and the amount of hours to wait between reviews
  private val reviewDateCalculator = Map(2 -> 8, 3 -> 24, 4 -> 48, 5 -> 7 * 24, 6 -> 14 * 24, 7 -> 30 * 24, 8 -> 120 * 24).withDefaultValue(4)
  //private val items = TableQuery[Deck]
  val tq = TableQuery[Deck]((tag: Tag) => new Deck(tag)("deckName"))

  def addItems(itemsToInsert: Seq[ItemEntry]): Future[Option[Int]] = {
    db.run(tq ++= itemsToInsert)
  }

  def getItems: Future[Seq[ItemEntry]] = {
    db.run(tq.result)
  }
}
