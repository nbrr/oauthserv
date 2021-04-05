package eu.nbrr.oauthserv.types.token

import eu.nbrr.oauthserv.types.token.TokenResponseEncoders._
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.http4s.Status.{BadRequest, Ok}
import org.http4s.{Response, Uri}
import org.http4s.circe.jsonEncoder

object TokenResponseEncoders {
  // FIXME omit None values
  implicit val encodeTokenResponse: Encoder[TokenResponse] = Encoder.instance {
    case trs@TokenResponseSuccess(_) => trs.asJson
    case tre@TokenResponseError(_, _, _) => tre.asJson
  }

  implicit val encodeTokenResponseSuccess: Encoder[TokenResponseSuccess] =
    Encoder.forProduct4(
      "access_token",
      /*"token_type",*/
      "expires_in",
      "refresh_token",
      "scope")(trs =>
      (trs.token.accessToken.toString,
        /*,*/
        trs.token.validity.toString, // FIXME should be optional
        trs.token.refreshToken.map(_.toString),
        trs.token.scope.mkString(",") // FIXME should be optional
      )) // FIXME time string format

  implicit val encodeTokenResponseError: Encoder[TokenResponseError] =
    Encoder.forProduct3("error", "error_description", "error_uri")(tre =>
      (tre.error.toString, tre.description.toString, tre.uri.toString)
    )
}


sealed trait TokenResponseErrorType

final case class InvalidRequest() extends TokenResponseErrorType {
  override def toString: String = "invalid_request"
}

final case class InvalidClient() extends TokenResponseErrorType {
  override def toString: String = "invalid_client"
}

final case class InvalidGrant() extends TokenResponseErrorType {
  override def toString: String = "invalid_grant"
}

final case class UnauthorizedClient() extends TokenResponseErrorType {
  override def toString: String = "unauthorized_client"
}

final case class UnsupportedGrantType() extends TokenResponseErrorType {
  override def toString: String = "unsupported_grant"
}

final case class InvalidScope() extends TokenResponseErrorType {
  override def toString: String = "invalid_scope"
}

sealed trait TokenResponse {
  def response[F[_]](): Response[F]
}

case class TokenResponseSuccess(token: Token) extends TokenResponse {
  override def response[F[_]](): Response[F] = Response[F](status = Ok).withEntity((this: TokenResponse).asJson)
}

case class TokenResponseError(error: TokenResponseErrorType, description: Option[ErrorDescription], uri: Option[ErrorUri]) extends TokenResponse {
  override def response[F[_]](): Response[F] = Response[F](status = BadRequest).withEntity((this: TokenResponse).asJson)
}

case class ErrorDescription(value: String) {
  override def toString: String = value
}

case class ErrorUri(value: Uri) {
  override def toString: String = value.toString
}

object TokenErrorUri {
  def apply(uri: Uri): ErrorUri = ErrorUri(uri)
}
