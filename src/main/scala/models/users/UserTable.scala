package models.users

import slick.lifted.Tag
import models.postgresProfile.MyPostgresProfile.api._

class UserTable(tag: Tag) extends Table[UserEntry](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc) //TODO value classes

  def login = column[String]("login")

  def salt = column[String]("salt")

  def password = column[String]("password")

  override def * = (id, login, salt, password).mapTo[UserEntry]
}
