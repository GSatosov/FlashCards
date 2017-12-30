package models.users

import models.ids.Ids.UserId

case class UserEntry(id: UserId = UserId(0L),
                     login: String,
                     salt: String,
                     password: String)