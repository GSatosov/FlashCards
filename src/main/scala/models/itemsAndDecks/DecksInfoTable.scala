package models.itemsAndDecks

import models.ids.Ids.{DeckId, UserId}
import models.postgresProfile.MyPostgresProfile.api._

class DecksInfoTable(tag: Tag) extends Table[DecksInfoEntry](tag, "decks") {

  def id = column[DeckId]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def authorId = column[UserId]("authorId")

  def basedOn = column[Option[DeckId]]("basedOn")

  override def * = (id, name, authorId, basedOn).mapTo[DecksInfoEntry]
}
