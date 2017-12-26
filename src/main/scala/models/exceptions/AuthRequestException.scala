package models.exceptions

import io.circe.syntax._

import io.circe.generic.semiauto._

object AuthRequestException {

  final case class SignUpException(message: String) {
    def encode = {
      implicit val encoder = deriveEncoder[SignUpException]
      this.asJson
    }

  }

  final case class LogInException(message: String) {
    def encode = {
      implicit val encoder = deriveEncoder[LogInException]
      this.asJson
    }
  }

}