package eu.nbrr.oauthserv.types

import org.http4s.Uri

import java.time.{Duration, Instant}
import java.util.UUID

case class AuthorizationCode(value: UUID)

case class AuthorizationState(value: String)

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
