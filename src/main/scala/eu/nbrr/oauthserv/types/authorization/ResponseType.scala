package eu.nbrr.oauthserv.types.authorization

sealed trait ResponseType

case class AuthorizationCode() extends ResponseType {
  override def toString: String = "authorization_code"
}

case class Token() extends ResponseType {
  override def toString: String = "token"
}
