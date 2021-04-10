package eu.nbrr.oauthserv.types.endpoints.token

sealed trait ErrorType

final case class InvalidRequest() extends ErrorType {
  override def toString: String = "invalid_request"
}

final case class InvalidClient() extends ErrorType {
  override def toString: String = "invalid_client"
}

final case class InvalidGrant() extends ErrorType {
  override def toString: String = "invalid_grant"
}

final case class UnauthorizedClient() extends ErrorType {
  override def toString: String = "unauthorized_client"
}

final case class UnsupportedGrantType() extends ErrorType {
  override def toString: String = "unsupported_grant"
}

final case class InvalidScope() extends ErrorType {
  override def toString: String = "invalid_scope"
}