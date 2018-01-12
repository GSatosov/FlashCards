import actors.UserAuthActor.SignUpRequest
import services.AuthService
import models.postgresProfile.MyPostgresProfile.api._
import org.scalatest.{AsyncFunSuite, BeforeAndAfter, Matchers}


class AuthServiceSpec extends AsyncFunSuite with Matchers with BeforeAndAfter {

  val authService = AuthServiceSpec.createAuthService
  test("Passing a correct sign up request should create the user in the database") {
    authService.handleSignUpRequest(SignUpRequest("test", "pass", "pass")).map {
      result => {
        assert(result.isRight)
      }
    }
  }
  test("Passed user's information is stored in the database") {
    authService.findUserByLogin("test").map(result => {
      assert(result.get.login == "test" && result.get.id.value == 1)
    })
  }
}

object AuthServiceSpec {
  def createAuthService = {
    implicit val db = Database.forConfig("inMemoryTestDB")
    import scala.concurrent.ExecutionContext.Implicits.global
    new AuthService
  }
}