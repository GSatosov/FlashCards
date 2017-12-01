package models

object Exceptions {

  sealed trait Exception

  final case class JSONParsingException(message: String) extends Exception

  final case class ItemParsingWarning(message: String) extends Exception

  final case class ItemParsingException(message: String) extends Exception

}
