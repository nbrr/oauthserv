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

case class Unsupported() extends GrantType
// FIXME figure out how to pass a parse error in the parameters instead of using this

object GrantType {
  def fromString(s: String): GrantType = s match {
    case "authorization_code" => AuthorizationCode()
    case "token" => Implicit()
    case "password" => ResourceOwnerPasswordCredentials()
    case "client_credentials" => ClientCredentials()
    case _ => Unsupported()
  }
}