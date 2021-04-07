package eu.nbrr.oauthserv.types.authorization

import eu.nbrr.oauthserv.types.{ClientId, ResourceOwner, Scope}
import org.http4s.Uri

import java.time.{Duration, Instant}
import java.util.UUID

case class AuthorizationCode(value: UUID) {
  override def toString: String = value.toString
}

object AuthorizationCode {
  def apply(s: String): AuthorizationCode = fromString(s)

  def fromString(s: String) = AuthorizationCode(UUID.fromString(s))
}

case class AuthorizationState(value: String) {
  override def toString: String = value
}

case class Authorization(
                          code: AuthorizationCode,
                          clientId: ClientId,
                          redirectionUri: Uri,
                          scope: Option[Scope],
                          state: Option[AuthorizationState],
                          date: Instant,
                          validity: Duration,
                          redeemed: Boolean,
                          resourceOwner: ResourceOwner)


sealed trait AuthorizationErrorType

final case class InvalidRequest() extends AuthorizationErrorType {
  override def toString: String = "invalid_request"
}

final case class UnauthorizedClient() extends AuthorizationErrorType {
  override def toString: String = "unauthorized_client"
}

final case class AccessDenied() extends AuthorizationErrorType {
  override def toString: String = "access_denied"
}

final case class UnsupportedResponseType() extends AuthorizationErrorType {
  override def toString: String = "unsupported_response_type"
}

final case class InvalidScope() extends AuthorizationErrorType {
  override def toString: String = "invalid_scope"
}

final case class ServerError() extends AuthorizationErrorType {
  override def toString: String = "server_error"
}


final case class TemporarilyUnavailable() extends AuthorizationErrorType {
  override def toString: String = "temporarily_unavailable"
}


case class ErrorDescription(value: String) {
  override def toString: String = value
}

case class ErrorUri(value: Uri) {
  override def toString: String = value.toString
}
