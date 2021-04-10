package eu.nbrr.oauthserv.types.token

case class TokenType(value: String) {
  override def toString: String = value
}
