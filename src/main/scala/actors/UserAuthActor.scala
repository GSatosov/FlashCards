package actors

import actors.UserAuthActor.{LogInRequest, SignUpRequest}
import akka.actor.{Actor, ActorLogging, Props}
import models.exceptions.AuthRequestException.{LogInException, SignUpException}
import models.ids.Ids.UserId
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}


class UserAuthActor(implicit authService: AuthService, executionContext: ExecutionContext) extends Actor with ActorLogging {
  override def receive: Receive = {
    case request@LogInRequest(username, _) =>
      log.info("Received login request for: " + username)
      sender() ! authService.handleLoginRequest(request)

    case request@SignUpRequest(username, _, _) =>
      log.info("Received sign up request for: " + username)
      sender ! authService.handleSignUpRequest(request)
  }


}

object UserAuthActor {
  def props(implicit authService: AuthService, executionContext: ExecutionContext) = Props(new UserAuthActor)

  trait AuthRequest

  case class LogInRequest(username: String, password: String) extends AuthRequest

  case class SignUpRequest(username: String, password: String, confirmedPassword: String) extends AuthRequest

}

