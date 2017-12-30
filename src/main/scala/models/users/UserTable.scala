package models.users

import models.ids.Ids.UserId
import models.postgresProfile.MyPostgresProfile.api._

class UserTable(tag: Tag) extends Table[UserEntry](tag, "users") {
  def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)

  def login = column[String]("login")

  def salt = column[String]("salt")

  def password = column[String]("password")

  override def * = (id, login, salt, password).mapTo[UserEntry]
}
