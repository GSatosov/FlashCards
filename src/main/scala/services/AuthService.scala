package services


import actors.UserAuthActor.{LogInRequest, SignUpRequest}
import models.exceptions.AuthRequestException.{LogInException, SignUpException}
import models.users.{UserEntry, UserTable}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class AuthService(implicit db: PostgresProfile.backend.Database, executionContext: ExecutionContext) {
  private val users = TableQuery[UserTable]
  createSchemaIfNotExists

  private def getUsers: Future[Seq[UserEntry]] = {
    db.run(users.result)
  }

  private def createSchemaIfNotExists: Unit = {
    db.run(MTable.getTables("users")).foreach(tables => if (tables.isEmpty) db.run(users.schema.create))
  }

  def handleSignUpRequest(request: SignUpRequest): Future[Either[Seq[SignUpException], Long]] = {
    val salt = createSalt
    val encodedPassword = encodePassword(request.password, salt)
    val userEntry = UserEntry(login = request.username, salt = salt, password = encodedPassword)
    db.run(users returning users.map(_.id) += userEntry).map(userId => Right(userId))
  }

  def handleLoginRequest(request: LogInRequest): Future[Either[Seq[LogInException], Long]] = {
    val maybeUser = findUserByLogin(request.username)
    maybeUser.map {
      case None => Left(Seq(LogInException("Wrong credentials.")))
      case Some(user) =>
        if (user.password == encodePassword(request.password, user.salt))
          Right(user.id)
        else
          Left(Seq(LogInException("Wrong credentials.")))
    }
  }

  def findUserByLogin(username: String) = {
    db.run(users.filter(i => i.login === username).result.headOption)
  }

  private def encodePassword(password: String, saltedString: String) = {
    String.format("%064x", new java.math.BigInteger(1,
      java.security.MessageDigest.getInstance("SHA-256").digest((password + saltedString).getBytes("UTF-8"))))
  }

  private def createSalt: String = {
    val toBeSalt = new Array[Byte](32)
    Random.nextBytes(toBeSalt)
    toBeSalt.map("%02x".format(_)).mkString
  }
}
