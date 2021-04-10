package eu.nbrr.oauthserv.types.authorization

import eu.nbrr.oauthserv.types.{ResourceOwner, authorization, client}
import org.http4s.Uri

import java.time.{Duration, Instant}

case class Authorization(
                          code: authorization.Code,
                          clientId: client.Id,
                          redirectionUri: Uri,
                          scope: Option[client.Scope],
                          state: Option[authorization.State],
                          date: Instant,
                          validity: Duration,
                          redeemed: Boolean,
                          resourceOwner: ResourceOwner)
