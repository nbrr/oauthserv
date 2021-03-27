package eu.nbrr.oauthserv.types.token

import eu.nbrr.oauthserv.types.Scope
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.http4s.Uri

import java.time.Duration


sealed trait TokenResponse

case class TokenResponseSuccess(accessToken: AccessToken,
                                // tokenType: TokenType,
                                expiresIn: Option[Duration],
                                refreshToken: Option[RefreshToken],
                                scope: Option[List[Scope]]) extends TokenResponse

object TokenResponseEncoders {
  // FIXME omit None values
  implicit val encodeTokenResponse: Encoder[TokenResponse] = Encoder.instance {
    case trs@TokenResponseSuccess(_, _, _, _) => trs.asJson
    case tre@TokenResponseError(_, _, _) => tre.asJson
  }

  implicit val encodeTokenResponseSuccess: Encoder[TokenResponseSuccess] =
    Encoder.forProduct4("access_token", /*"token_type",*/ "expires_in", "refresh_token", "scope")(trs =>
      (trs.accessToken.toString, /*,*/ trs.expiresIn.map(_.toString), trs.refreshToken.map(_.toString), trs.scope.map(_.mkString(",")))
    ) // FIXME time string format

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

case class TokenResponseError(error: TokenResponseErrorType, description: Option[ErrorDescription], uri: Option[ErrorUri]) extends TokenResponse

case class ErrorDescription(value: String) {
  override def toString: String = value
}

case class ErrorUri(value: Uri) {
  override def toString: String = value.toString
}

object TokenErrorUri {
  def apply(uri: Uri): ErrorUri = ErrorUri(uri)
}
