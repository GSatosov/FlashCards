package models

import models.JsonParsingException._


/**
  * Class representing items that are to be reviewed.
  *
  * @param text        A term you want to memorize
  * @param description Definition of the term or mnemonics for the term
  * @param precedence  This value determines how early you will see this item in chain of reviews.
  *
  */

case class Item(text: Option[String], level: Option[Int], meaningVariants: Option[List[String]], readingVariants: Option[List[String]], description: Option[String], precedence: Option[Int]) {
  def validate: Option[ItemParsingException] = {
    if (valueNotExists(text) || (listNotExists(meaningVariants) && listNotExists(readingVariants) && valueNotExists(description)))
      Some(MajorItemParsingException(constructExceptionMessage))
    else if (listNotExists(meaningVariants) || listNotExists(readingVariants) || valueNotExists(description))
      Some(MinorItemParsingException(constructExceptionMessage))
    else
      None
  }

  private def constructExceptionMessage: String = {
    val base = "there is no"
    val elements = List((meaningVariants, "meaning variants"), (readingVariants, "reading variants")).map { case (field, name) => if (listNotExists(field)) name else "" } ++
      List((description, "description"), (text, "text")).map { case (field, name) => if (valueNotExists(field)) name else "" }
    elements.count(_.nonEmpty) match {
      case 1 => base + elements.head
      case 2 => base + elements.head + " and " + elements.last
      case _ => elements.tail.init.foldLeft(base + elements.head)((a, b) => a.concat(", ").concat(b)) + " and " + elements.last
    }
  }

  private def listNotExists(field: Option[List[String]]): Boolean = {
    field.isEmpty || field.get.isEmpty || field.forall(x => x.isEmpty)
  }

  private def valueNotExists(field: Option[String]): Boolean = {
    field.isEmpty || field.get.isEmpty
  }

}
