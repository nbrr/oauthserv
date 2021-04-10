package eu.nbrr.oauthserv.types.endpoints.authentication

sealed trait AuthenticationErrorType

final case class InvalidRequest() extends AuthenticationErrorType {
  override def toString: String = "invalid_request"
}

final case class UnauthorizedClient() extends AuthenticationErrorType {
  override def toString: String = "unauthorized_client"
}

final case class AccessDenied() extends AuthenticationErrorType {
  override def toString: String = "access_denied"
}

final case class UnsupportedResponseType() extends AuthenticationErrorType {
  override def toString: String = "unsupported_response_type"
}

final case class InvalidScope() extends AuthenticationErrorType {
  override def toString: String = "invalid_scope"
}

final case class ServerError() extends AuthenticationErrorType {
  override def toString: String = "server_error"
}


final case class TemporarilyUnavailable() extends AuthenticationErrorType {
  override def toString: String = "temporarily_unavailable"
}
