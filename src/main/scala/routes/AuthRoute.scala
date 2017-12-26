package routes


import actors.UserAuthActor.{LogInRequest, SignUpRequest}
import akka.actor.ActorRef
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives.{as, concat, entity, onComplete}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import akka.util.Timeout
import io.circe.generic.auto._
import com.softwaremill.session.CsrfDirectives._
import com.softwaremill.session.CsrfOptions._
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import models.exceptions.AuthRequestException.SignUpException
import models.sessions.{Session, SessionSupport}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthRoute(implicit mainActor: ActorRef, executionContext: ExecutionContext) extends SessionSupport {

  implicit lazy val timeout = Timeout(5 seconds)
  lazy val routes: Route =
    path("login") {
      concat(
        post {
          entity(as[LogInRequest]) { request: LogInRequest =>
            val response = (mainActor ? request).mapTo[Future[Either[Seq[String], Long]]]
            onComplete(response.flatten) {
              case Failure(err) => complete(err.getMessage)
              case Success(value) => value match {
                case Left(errors) => complete(errors)
                case Right(userId) =>
                  setSession(refreshable, usingCookies, Session(userId.toString)) {
                    setNewCsrfToken(checkHeader) { ctx => ctx.complete("Login successful") }
                  }
              }
            }
          }
        }, get {
          requiredSession(refreshable, usingCookies) { session => //For testing purposes
            ctx =>
              println(session)
              ctx.complete(session.username)
          }
        }
      )
    } ~ path("signUp") {
      post {
        entity(as[SignUpRequest]) { signUpRequest =>
          val response = (mainActor ? signUpRequest).mapTo[Future[Either[Seq[SignUpException], Long]]]
          onComplete(response.flatten) {
            case Failure(err) => complete(err.getMessage)
            case Success(errorsOrUser) => errorsOrUser match {
              case Left(errors) => println(errors)
                complete(errors)
              case Right(userId) => setSession(refreshable, usingCookies, Session(userId.toString)) {
                setNewCsrfToken(checkHeader) { ctx => ctx.complete("Sign-up successful. Session set") }
              }
            }
          }
        }
      }
    }
}