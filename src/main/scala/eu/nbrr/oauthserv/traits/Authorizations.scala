package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types._
import org.http4s.Uri

trait Authorizations {
  def create(clientId: ClientId, redirectionUri: Uri, scopes: List[Scope], state: AuthorizationState, resourceOwner: ResourceOwner): Authorization

  def findByCode(code: AuthorizationCode): Option[Authorization]
}


