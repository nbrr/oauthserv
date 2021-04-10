package eu.nbrr.oauthserv.types.endpoints.token

sealed trait TokenErrorType

final case class InvalidRequest() extends TokenErrorType {
  override def toString: String = "invalid_request"
}

final case class InvalidClient() extends TokenErrorType {
  override def toString: String = "invalid_client"
}

final case class InvalidGrant() extends TokenErrorType {
  override def toString: String = "invalid_grant"
}

final case class UnauthorizedClient() extends TokenErrorType {
  override def toString: String = "unauthorized_client"
}

final case class UnsupportedGrantType() extends TokenErrorType {
  override def toString: String = "unsupported_grant"
}

final case class InvalidScope() extends TokenErrorType {
  override def toString: String = "invalid_scope"
}