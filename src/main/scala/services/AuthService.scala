package services


import actors.UserAuthActor.{LogInRequest, SignUpRequest}
import models.exceptions.AuthRequestException.{LogInException, SignUpException}
import models.ids.Ids.UserId
import models.postgresProfile.MyPostgresProfile
import models.users.{UserEntry, UserTable}

import models.postgresProfile.MyPostgresProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

class AuthService(implicit db: MyPostgresProfile.backend.Database, executionContext: ExecutionContext) {
  private val users = TableQuery[UserTable]
  createSchemaIfNotExists

  private def createSchemaIfNotExists: Unit = {
    val creationQuery = db.run(MTable.getTables("users")).flatMap(tables => if (tables.isEmpty) db.run(users.schema.create) else Future.successful())
    Await.result(creationQuery, Duration.Inf)
    println("created schema")
  }

  private def createUser(request: SignUpRequest): Future[Right[Seq[SignUpException], UserId]] = {
    val salt = createSalt
    val encodedPassword = encodePassword(request.password, salt)
    val userEntry = UserEntry(login = request.username, salt = salt, password = encodedPassword)
    db.run(users returning users.map(_.id) += userEntry).map(userId => Right(userId))
  }

  def handleSignUpRequest(request: SignUpRequest): Future[Either[Seq[SignUpException], UserId]] = {
    val maybeErrors = validateSignUpRequest(request)
    maybeErrors.flatMap {
      case Nil => createUser(request)
      case seq => Future.successful(Left(seq))
    }
  }

  private def validateLogInRequest(request: LogInRequest): Seq[LogInException] = {
    val maybeErrors = List(validateUserName(request.username), validatePassword(request.password))
    maybeErrors.flatten.map(LogInException)
  }

  private def validateSignUpRequest(request: SignUpRequest): Future[Seq[SignUpException]] = {
    val maybeUsernameErrorFuture = validateUserName(request.username).fold(findUserByLogin(request.username).map {
      _.map(_ => "This username is already occupied.")
    })(error => Future.successful(Some(error)))
    for {
      maybeUsernameError <- maybeUsernameErrorFuture
      maybePasswordError = validatePasswords(request.password, request.confirmedPassword)
    }
      yield Seq(maybeUsernameError, maybePasswordError).flatten.map(SignUpException)
  }

  private def validateUserName(username: String) = username match {
    case "" => Some("Username must not be empty")
    case _ => None
  }

  private def validatePassword(password: String) = {
    password match {
      case "" => Some("Please specify your password")
      case _ => None
    }
  }

  private def validatePasswords(password: String, confirmedPassword: String) = {
    (password, confirmedPassword) match {
      case ("", "") => Some("Please specify your password and confirm it.")
      case ("", _) => Some("Please specify your password")
      case (_, "") => Some("Please confirm your password")
      case (pass, confirmedPass) if pass != confirmedPass => Some("Passwords should match")
      case _ => None
    }
  }

  def handleLoginRequest(request: LogInRequest): Future[Either[Seq[LogInException], UserId]] = {
    val maybeErrors = validateLogInRequest(request)
    maybeErrors match {
      case Nil => authenticateLogIn(request)
      case seq => Future.successful(Left(seq))
    }
  }

  private def authenticateLogIn(request: LogInRequest) = {
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

  def findUserByLogin(username: String): Future[Option[UserEntry]] =
    db.run(users.filter(i => i.login === username).result.headOption)

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
