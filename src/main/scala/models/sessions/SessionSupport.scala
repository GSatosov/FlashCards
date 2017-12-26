package models.sessions

import com.softwaremill.session._
trait SessionSupport {
  implicit val sessionEncoder: BasicSessionEncoder[Session] = new BasicSessionEncoder[Session]()
  val sessionConfig: SessionConfig = SessionConfig.default(SessionUtil.randomServerSecret())
  implicit val sessionManager: SessionManager[Session] = new SessionManager[Session](sessionConfig)

  implicit val refreshTokenStorage: InMemoryRefreshTokenStorage[Session] {
    def log(msg: String): Unit
  } = new InMemoryRefreshTokenStorage[Session] {
    def log(msg: String) = println(msg)
  }


}
