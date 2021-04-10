package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.authorization.Authorization
import eu.nbrr.oauthserv.types.resource_owner.ResourceOwner
import org.http4s.Uri

trait Authorizations {
  def create(clientId: client.Id,
             redirectionUri: Uri,
             scope: Option[client.Scope],
             state: Option[authorization.State],
             resourceOwner: ResourceOwner): Authorization

  def findByCode(code: authorization.Code): Option[Authorization]
}
