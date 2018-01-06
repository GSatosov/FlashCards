package routes

import actors.ItemsAndDecksActor.{CreateDeckRequest, GetDecks, GetDecksByUserId}
import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives.{as, entity, onComplete}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import akka.util.Timeout
import io.circe.generic.auto._

import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._

import models.exceptions.DeckParsingException

import models.ids.Ids.{DeckId, UserId}
import models.sessions.{Session, SessionSupport}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class DeckRoute(implicit mainActor: ActorRef, executionContext: ExecutionContext) extends SessionSupport {
  implicit val timeout = Timeout(5 seconds)
  lazy val routes: Route =
    path("decks") {
      get {
        val response = (mainActor ? GetDecks).mapTo[Future[Seq[(String, String)]]] //Returns deck names and logins of their authors
        onComplete(response.flatten) {
          case Failure(err) => complete(err.getMessage)
          case Success(value) => complete(value)
        }
      }
    } ~
      pathPrefix("my_decks") {
        get {
          requiredSession(refreshable, usingCookies) { session =>
              val response = (mainActor ? GetDecksByUserId(UserId(session.id.toLong))).mapTo[Future[Seq[(DeckId, String)]]]
              onComplete(response.flatten) {
                case Failure(err) => complete(err.getMessage)
                case Success(value) => complete(value)
              }
          }
        } ~
          path("new") {
            get {
              requiredSession(refreshable, usingCookies) { session =>
                ctx => ctx.complete("Form for creating a deck (field for a name and items)")
              }
            } ~
              post { //Validate name, redirect to created deck's url to add items. TODO add privacy settings
                requiredSession(refreshable, usingCookies) { session =>
                  entity(as[CreateDeckRequest]) { request =>
                    val response = (mainActor ? request).mapTo[Future[Either[DeckParsingException, DeckId]]]
                    onComplete(response.flatten) {
                      case Failure(err) => complete(err.getMessage)
                      case Success(value) => value match {
                        case Left(error) => complete(error)
                        case Right(id) => redirect("my_decks/" + id.value, StatusCodes.PermanentRedirect)
                      }
                    }
                  }
                }
              } ~
              path(IntNumber) { deckId => //Endpoint for accessing a specific deck.
                complete(deckId)
              }
          }
      }
}