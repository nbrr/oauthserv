package eu.nbrr.oauthserv.types.endpoints.token

import org.http4s.FormDataDecoder.FormData

sealed trait GrantTypeData

object GrantTypeData {
  def eitherFromStringAndData(s: String, data: FormData): Either[String, GrantTypeData] = s match {
    case "authorization_code" => Right(AuthorizationCodeData(data))
    case "password" => Right(ResourceOwnerPasswordCredentialsData(data))
    case "client_credentials" => Right(ClientCredentialsData(data))
    case _ => Left(s)
  }
}

case class AuthorizationCodeData(d: FormData) extends GrantTypeData

case class ResourceOwnerPasswordCredentialsData(d: FormData) extends GrantTypeData

case class ClientCredentialsData(d: FormData) extends GrantTypeData
