package services

import io.circe.{Decoder, Json}
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.parse
import models.exceptions.JsonParsingException.{ItemParsingException, JSONParsingException, MajorItemParsingException, MinorItemParsingException}
import models.itemsAndDecks.ParsedItem

object ItemParser {
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

}
