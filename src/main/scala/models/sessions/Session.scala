package models.sessions

import com.softwaremill.session.{SessionSerializer, SingleValueSessionSerializer}

import scala.util.Try

case class Session(id: String)


object Session {
  implicit def serializer: SessionSerializer[Session, String] = new SingleValueSessionSerializer(
    _.id,
    (un: String) => Try {
      Session(un)
    })
}