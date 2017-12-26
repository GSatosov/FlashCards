package actors

import actors.UserAuthActor.{LogInRequest, SignUpRequest}
import akka.actor.{Actor, ActorLogging, Props}
import models.exceptions.AuthRequestException.{LogInException, SignUpException}
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}

class UserAuthActor(implicit authService: AuthService, executionContext: ExecutionContext) extends Actor with ActorLogging {
  override def receive: Receive = {
    case request@LogInRequest(username, _) =>
      log.info("Received login request for: " + username)
      val maybeErrors = validateLogInRequest(request)
      val response: Future[Either[Seq[LogInException], Long]] = maybeErrors match {
        case Nil => authService.handleLoginRequest(request)
        case seq => Future.successful(Left(seq))
      }
      sender() ! response

    case request@SignUpRequest(username, _, _) =>
      log.info("Received sign up request for: " + username)
      val maybeErrors = Future.sequence(validateSignUpRequest(request))
      val response: Future[Either[Seq[SignUpException], Long]] = maybeErrors.flatMap(sequence =>
        sequence.flatten match {
          case Nil => authService.handleSignUpRequest(request)
          case seq => Future.successful(Left(seq.filterNot(_.isEmpty).map(SignUpException)))
        }
      )
      sender ! response
  }


  private def validateLogInRequest(request: LogInRequest): Seq[LogInException] = {
    val maybeErrors = List(validateUserName(request.username), validatePassword(request.password))
    maybeErrors.flatten.map(LogInException)
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
      case (pass, confirmedPass) => if (pass != confirmedPass) Some("Passwords should match") else None
    }
  }

  private def validateSignUpRequest(request: SignUpRequest): Seq[Future[Option[String]]] = {
    val errors = List(validateUserName(request.username),
      validatePasswords(request.password,
        request.confirmedPassword)).map(Future.successful)
    println(errors)
    authService.findUserByLogin(request.username).map {
      case Some(_) => Some("This username is already occupied.")
      case None => None
    } :: errors
  }
}

object UserAuthActor {
  def props(implicit authService: AuthService, executionContext: ExecutionContext) = Props(new UserAuthActor)

  trait AuthRequest

  case class LogInRequest(username: String, password: String) extends AuthRequest

  case class SignUpRequest(username: String, password: String, confirmedPassword: String) extends AuthRequest


}

