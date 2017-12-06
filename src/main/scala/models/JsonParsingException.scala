package models

object JsonParsingException {

  sealed trait ItemParsingException

  final case class JSONParsingException(message: String)

  final case class MinorItemParsingException(message: String) extends ItemParsingException

  final case class MajorItemParsingException(message: String) extends ItemParsingException

}
