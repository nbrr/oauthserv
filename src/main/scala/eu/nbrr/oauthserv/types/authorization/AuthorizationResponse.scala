package eu.nbrr.oauthserv.types.authorization

import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.http4s.Uri

// FIXME unnecessary refactor due to case class name conflict with TokenResponse for errors ?

sealed trait AuthorizationResponse

case class AuthorizationResponseSuccess(code: AuthorizationCode,
                                        state: AuthorizationState) extends AuthorizationResponse

object AuthorizationResponseEncoders {
  // FIXME omit None values
  implicit val encodeTokenResponse: Encoder[AuthorizationResponse] = Encoder.instance {
    case ars@AuthorizationResponseSuccess(_, _) => ars.asJson
    case are@AuthorizationResponseError(_, _, _, _) => are.asJson
  }

  implicit val encodeTokenResponseSuccess: Encoder[AuthorizationResponseSuccess] =
    Encoder.forProduct2("code", "state")(ars =>
      (ars.code.toString, ars.state.toString)
    )

  implicit val encodeTokenResponseError: Encoder[AuthorizationResponseError] =
    Encoder.forProduct4("error", "error_description", "error_uri", "state")(are =>
      (are.error.toString, are.description.toString, are.uri.toString, are.state.toString)
    )
}

sealed trait AuthorizationResponseErrorType

final case class InvalidRequest() extends AuthorizationResponseErrorType {
  override def toString: String = "invalid_request"
}

final case class UnauthorizedClient() extends AuthorizationResponseErrorType {
  override def toString: String = "unauthorized_client"
}

final case class AccessDenied() extends AuthorizationResponseErrorType {
  override def toString: String = "access_denied"
}

final case class UnsupportedResponseType() extends AuthorizationResponseErrorType {
  override def toString: String = "unsupported_response_type"
}

final case class InvalidScope() extends AuthorizationResponseErrorType {
  override def toString: String = "invalid_scope"
}

final case class ServerError() extends AuthorizationResponseErrorType {
  override def toString: String = "server_error"
}


final case class TemporarilyUnavailable() extends AuthorizationResponseErrorType {
  override def toString: String = "temporarily_unavailable"
}

case class AuthorizationResponseError(error: AuthorizationResponseErrorType, description: Option[ErrorDescription], uri: Option[ErrorUri], state: AuthorizationState) extends AuthorizationResponse

case class ErrorDescription(value: String) {
  override def toString: String = value
}

case class ErrorUri(value: Uri) {
  override def toString: String = value.toString
}
