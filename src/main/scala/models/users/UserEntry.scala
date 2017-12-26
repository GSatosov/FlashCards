package models.users


case class UserEntry(id: Long = 0L,
                     login: String,
                     salt: String,
                     password: String) {

}
