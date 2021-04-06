package eu.nbrr.oauthserv.types

import org.http4s.Uri

import java.util.UUID

case class ClientId(value: UUID) {
  override def toString: String = value.toString
}

object ClientId {
  def apply(s: String): ClientId = fromString(s)

  def fromString(s: String) = ClientId(UUID.fromString(s))
}

sealed trait ClientType

final case class PublicClient() extends ClientType

final case class ConfidentialClient() extends ClientType

case class ClientSecret(value: String)

case class Scope(value: String) {
  override def toString: String = value
}

object Scope {
  def apply(s: String): Scope = Scope(s)
}

case class Scopes(value: List[Scope]) {
  override def toString: String = value.mkString(" ")
}

object Scopes {
  def apply(s: String): Scopes = fromString(s)

  def fromString(s: String): Scopes = Scopes(s.split(" ").map(Scope(_)).toList)
}

case class RegisteredClient(
                             id: ClientId,
                             _type: ClientType,
                             secret: ClientSecret,
                             scopes: Scopes,
                             redirectionUri: Uri)
