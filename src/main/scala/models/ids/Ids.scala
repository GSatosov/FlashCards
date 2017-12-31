package models.ids

import slick.lifted.MappedTo

object Ids {

  case class UserId(value: Long) extends AnyVal with MappedTo[Long]

  case class ItemId(value: Long) extends AnyVal with MappedTo[Long]

  case class DeckId(value: Long) extends AnyVal with MappedTo[Long]

}
