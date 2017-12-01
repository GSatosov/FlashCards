package services

import io.circe._
import io.circe.parser._
import models.Item
import models.Exceptions._

import scala.util.Try

object ItemParser {
  /**
    * Parsed JSON from String
    *
    * @param string JSON String
    * @return Exception if JSON cannot be parsed and Sequence of Either major exceptions or Items with minor exceptions
    */
  def makeJSONOutOfString(string: String): Either[Exception, Vector[(Option[Item], Option[Exception])]] = parse(string) match {
    case Left(err) => Left(JSONParsingException(err.message))
    case Right(json) => Right(parseJSON(json))
  }

  private def parseJSON(json: Json): Vector[(Option[Item], Option[Exception])] = {
    val cursor = json.hcursor
    val topLevelFields = cursor.fields.get
    topLevelFields.flatMap(levelName => {
      val levelNumber = Try(levelName.substring(5).toInt) //Level01 -> 1
      if (levelNumber.isFailure)
        List((None, Some(ItemParsingException(s"No Level number in field $levelName."))))
      else {
        val levelCursor = cursor.downField(levelName).downArray
        if (levelCursor.failed)
          List((None, Some(ItemParsingException(s"No array for items in level $levelNumber."))))
        else
          parseLevel(levelCursor, levelNumber.get, 1)
      }
    })
  }

  private def parseLevel(cursor: ACursor, levelNumber: Int, index: Int): List[(Option[Item], Option[Exception])] = {
    val textField = cursor.downField("text").as[String]
    textField match {
      case (Left(_) | Right("")) =>
        if (!cursor.right.failed)
          (None, Some(ItemParsingException(s"No text for level $levelNumber item № $index."))) :: parseLevel(cursor.right, levelNumber, index + 1)
        else
          List((None, Some(ItemParsingException(s"No text for level $levelNumber item № $index."))))
      case Right(text) =>
        if (!cursor.right.failed)
          parseItem(cursor, text, levelNumber, index) :: parseLevel(cursor.right, levelNumber, index + 1)
        else
          List(parseItem(cursor, text, levelNumber, index))
    }
  }

  //TODO add check for correct JSON types
  private def parseItem(cursor: ACursor, text: String, levelNumber: Int, index: Int): (Option[Item], Option[Exception]) = {
    val translationVars = cursor.downField("translationVariants").as[List[String]].toOption
    val readingVars = cursor.downField("readingVariants").as[List[String]].toOption
    val description = cursor.downField("description").as[String].toOption
    val precedence = cursor.downField("precedence").as[Int].toOption

    val warningCandidates = List(
      checkIfListExistsAndIsNotEmpty(translationVars, "translation variants"),
      checkIfListExistsAndIsNotEmpty(readingVars, "reading variants"),
      checkIfValueExistsAndIsNotEmpty(description, "description"))

    val warning = constructExceptionMessage(warningCandidates.filter(x => x.nonEmpty)) + s" for level $levelNumber item№ $index, text: $text."
    if (warningCandidates.forall(!_.isEmpty)) {
      (None, Some(ItemParsingException(warning)))
    }
    else {
      val item = Some(new Item(text = text,
        levelNumber = levelNumber,
        translation = translationVars,
        reading = readingVars,
        description = description,
        precedence = precedence))
      if (warningCandidates.forall(_.isEmpty)) //All important fields are filled
        (item, None)
      else { //Some important fields are not filled
        (item, Some(ItemParsingWarning(warning)))
      }
    }
  }

  private def constructExceptionMessage(elements: List[String]): String = {
    val base = "There is no "
    elements.length match {
      case 1 => base + elements.head
      case 2 => base + elements.head + " and " + elements.last
      case _ => elements.tail.init.foldLeft(base + elements.head)((a, b) => a.concat(", ").concat(b)) + " and " + elements.last
    }
  }

  /**
    * Checks if field is empty (None, Some(List()) or Some(List("")) and returns fieldName to construct a warning if so
    *
    */
  private def checkIfListExistsAndIsNotEmpty(field: Option[List[String]], fieldName: String): String = {
    if (field.isEmpty || field.get.isEmpty || field.get.forall(x => x.isEmpty))
      fieldName
    else
      ""
  }

  /**
    * Same but for types not in list
    *
    */
  private def checkIfValueExistsAndIsNotEmpty(field: Option[String], fieldName: String): String = {
    if (field.isEmpty || field.get.isEmpty)
      fieldName
    else
      ""
  }
}