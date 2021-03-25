package eu.nbrr.oauthserv.types

import org.http4s.Uri

import java.util.UUID

case class ClientId(value: UUID)

sealed trait ClientType

final case class PublicClient() extends ClientType

final case class ConfidentialClient() extends ClientType

case class ClientSecret(value: String)

case class Scope(value: String)

case class RegisteredClient(
                             id: ClientId,
                             _type: ClientType,
                             secret: ClientSecret,
                             scopes: List[Scope],
                             redirectionUri: Uri)
