package eu.nbrr.oauthserv.types.endpoints.authorization

import org.http4s.Uri


sealed trait ErrorType

final case class InvalidRequest() extends ErrorType {
  override def toString: String = "invalid_request"
}

final case class UnauthorizedClient() extends ErrorType {
  override def toString: String = "unauthorized_client"
}

final case class AccessDenied() extends ErrorType {
  override def toString: String = "access_denied"
}

final case class UnsupportedResponseType() extends ErrorType {
  override def toString: String = "unsupported_response_type"
}

final case class InvalidScope() extends ErrorType {
  override def toString: String = "invalid_scope"
}

final case class ServerError() extends ErrorType {
  override def toString: String = "server_error"
}


final case class TemporarilyUnavailable() extends ErrorType {
  override def toString: String = "temporarily_unavailable"
}


case class ErrorDescription(value: String) {
  override def toString: String = value
}

case class ErrorUri(value: Uri) {
  override def toString: String = value.toString
}
