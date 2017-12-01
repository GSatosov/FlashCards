package models


/**
  * Class representing items that are to be reviewed.
  *
  * @param text        A term you want to memorize
  * @param description Definition of the term or mnemonics for the term
  * @param precedence  This value determines how early you will see this item in chain of reviews.
  *
  */

class Item(text: String, levelNumber: Int, translation: Option[List[String]], reading: Option[List[String]], description: Option[String], precedence: Option[Int]) {
  override def toString: String = s"Level№$levelNumber, item $text"
}
