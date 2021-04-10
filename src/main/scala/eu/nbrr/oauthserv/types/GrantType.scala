package eu.nbrr.oauthserv.types

sealed trait GrantType

case class AuthorizationCode() extends GrantType {
  val responseType: String = "code"
  val grantType: String = "authorization_code"
}

case class Implicit() extends GrantType {
  val responseType: String = "token"
}

case class ResourceOwnerPasswordCredentials() extends GrantType {
  val grantType: String = "password"
}

case class ClientCredentials() extends GrantType {
  val grantType: String = "client_credentials"
}
// TODO extensions
