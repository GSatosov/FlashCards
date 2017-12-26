package models.postgresProfile

import com.github.tminglei.slickpg.{ExPostgresProfile, PgArraySupport}
import slick.basic.Capability
import slick.driver.JdbcProfile


trait MyPostgresProfile extends ExPostgresProfile
  with PgArraySupport {

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcProfile.capabilities.insertOrUpdate

  override val api = MyAPI

  object MyAPI extends API with ArrayImplicits

}

object MyPostgresProfile extends MyPostgresProfile