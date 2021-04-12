package eu.nbrr.oauthserv.types

import org.http4s.FormDataDecoder.FormData

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

  def eitherFromString(s: String): Either[String, GrantType] = s match {
    case "authorization_code" => Right(AuthorizationCode())
    case "token" => Right(Implicit())
    case "password" => Right(ResourceOwnerPasswordCredentials())
    case "client_credentials" => Right(ClientCredentials())
    case _ => Left(s)
  }

  def maybeFromString(s: String): Option[GrantType] = s match {
    case "authorization_code" => Some(AuthorizationCode())
    case "token" => Some(Implicit())
    case "password" => Some(ResourceOwnerPasswordCredentials())
    case "client_credentials" => Some(ClientCredentials())
    case _ => None
  }
}


sealed trait GrantTypeData

case class AuthorizationCodeData(d: FormData) extends GrantTypeData

case class ResourceOwnerPasswordCredentialsData(d: FormData) extends GrantTypeData

object GrantTypeData {
  def eitherFromStringAndData(s: String, data: FormData): Either[String, GrantTypeData] = s match {
    case "authorization_code" => Right(AuthorizationCodeData(data))
    case "password" => Right(ResourceOwnerPasswordCredentialsData(data))
    case _ => Left(s)
  }
}