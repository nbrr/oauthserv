package eu.nbrr.oauthserv.types.authorization

import eu.nbrr.oauthserv.types.{ClientId, ResourceOwner, Scope}
import org.http4s.Uri

import java.time.{Duration, Instant}
import java.util.UUID

case class AuthorizationCode(value: UUID) {
  override def toString: String = value.toString
}

object AuthorizationCode {
  def apply(s: String): AuthorizationCode = fromString(s)

  def fromString(s: String) = AuthorizationCode(UUID.fromString(s))
}

case class AuthorizationState(value: String) {
  override def toString: String = value
}

case class Authorization(
                          code: AuthorizationCode,
                          clientId: ClientId,
                          redirectionUri: Uri,
                          scopes: List[Scope],
                          state: AuthorizationState,
                          date: Instant,
                          validity: Duration,
                          redeemed: Boolean,
                          resourceOwner: ResourceOwner)

