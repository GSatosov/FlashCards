package services

import io.circe._
import io.circe.parser._
import io.circe.Decoder
import models.Item
import models.JsonParsingException._
import io.circe.generic.semiauto._


object ItemParser {
  /**
    * Parses JSON from String
    *
    * @param string JSON String
    * @return Exception if JSON cannot be parsed and Sequence of Either major exceptions or Items with minor exceptions
    */
  def getItemsOutOfString(string: String): Either[JSONParsingException, Seq[Item]] = parse(string) match {
    case Left(err) => Left(JSONParsingException(err.message))
    case Right(json) => decodeJson(json)
  }

  def validateItems(items: Seq[Item]): Seq[(Option[Item], Option[ItemParsingException])] = {
    (1 to items.length).zip(items).map { case (index, item) =>
      if (item.validate.isEmpty)
        (Some(item), None)
      else item.validate.get match {
        case MajorItemParsingException(message) => (None, Some(MajorItemParsingException(s"$message for item $index.")))
        case MinorItemParsingException(message) => (Some(item), Some(MinorItemParsingException(s"$message for item $index.")))
      }
    }
  }

  private def decodeJson(json: Json): Either[JSONParsingException, Seq[Item]] = {
    implicit val decodeItem: Decoder[Item] = deriveDecoder[Item]
    json.hcursor.as[Seq[Item]] match {
      case Left(err) => Left(JSONParsingException(err.message))
      case Right(data) => Right(data)
    }
  }
}
