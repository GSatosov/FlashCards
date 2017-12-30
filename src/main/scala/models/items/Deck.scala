package models.items

import java.sql.Date

import models.ids.Ids.ItemId
import models.postgresProfile.MyPostgresProfile.api._

class Deck(tag: Tag)(name: String) extends Table[ItemEntry](tag, name) {
  def id = column[ItemId]("id", O.PrimaryKey, O.AutoInc)

  def text = column[String]("text")

  def level = column[Int]("level")

  def meaningVariants = column[List[String]]("meaningvariants")

  def readingVariants = column[List[String]]("readingvariants")

  def description = column[String]("description")

  def precedence = column[Int]("precedence")

  def knowledgeLevel = column[Int]("knowledgelevel")

  def unlockDate = column[Option[Date]]("unlockdate")

  def reviewDate = column[Option[Date]]("reviewdate")

  def * = (id, text, level, meaningVariants, readingVariants, description, precedence, knowledgeLevel, unlockDate, reviewDate).mapTo[ItemEntry]

}
