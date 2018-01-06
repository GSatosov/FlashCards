package models.exceptions

import io.circe.syntax._

import io.circe.generic.semiauto._

object AuthRequestException {

  final case class SignUpException(message: String)

  final case class LogInException(message: String)

}