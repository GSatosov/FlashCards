package routes

import akka.http.scaladsl.server.Route
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
import scala.concurrent.ExecutionContext.Implicits._
class DeckRoute extends SessionSupport {
  lazy val routes: Route =
    path("decks") {
      get {
        complete("Shows all decks. Users can subscribe to ones they like.")
      }
    } ~
      pathPrefix("my_decks") {
        get {
          requiredSession(refreshable, usingCookies) { session =>
            ctx => ctx.complete("This route shows decks and allows you to create one/add items to existing one.")
          }
        } ~
          path("new") {
            concat(
              get {
                requiredSession(refreshable, usingCookies) { session =>
                  ctx => ctx.complete("Form for creating a deck (field for a name and items)")
                }
              } ~
                post {
                  requiredSession(refreshable, usingCookies) { session =>
                    ctx => ctx.complete("Creates the deck") //entity(as[])
                  }
                }
            )
          }
      }

}