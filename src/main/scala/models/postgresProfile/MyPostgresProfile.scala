package models.postgresProfile

import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport}


trait MyPostgresProfile extends ExPostgresProfile
  with PgArraySupport {

  override val api = MyAPI

  object MyAPI extends API with ArrayImplicits

}

object MyPostgresProfile extends MyPostgresProfile