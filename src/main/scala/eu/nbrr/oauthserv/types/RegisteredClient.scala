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

case class Scope(value: Set[String]) {
  override def toString: String = value.mkString(" ")

  def get(): Set[String] = value

  def contains(scopeElt: String): Boolean = value.contains(scopeElt)

  def contains(scope: Scope): Boolean = scope.get().subsetOf(value)
}

object Scope {
  def apply(s: String): Scope = fromString(s)

  def fromString(s: String): Scope = Scope(s.split(" ").toSet)
}

case class RegisteredClient(
                             id: ClientId,
                             _type: ClientType,
                             secret: ClientSecret,
                             scope: Scope,
                             redirectionUri: Uri)
