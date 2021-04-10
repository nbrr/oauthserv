package eu.nbrr.oauthserv.types.endpoints.authentication

import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.resource_owner.{RoId, RoSecret}
import org.http4s.Uri

case class AuthenticationRequest(roId: RoId,
                                 roSecret: RoSecret,
                                 clientId: client.Id,
                                 redirectionUri: Uri,
                                 state: Option[authorization.State],
                                 scope: Option[client.Scope])
