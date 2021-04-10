package eu.nbrr.oauthserv.impls

import eu.nbrr.oauthserv.traits.Authorizations
import eu.nbrr.oauthserv.types.authorization.Authorization
import eu.nbrr.oauthserv.types.resource_owner.ResourceOwner
import eu.nbrr.oauthserv.types.{authorization, _}
import org.http4s.Uri

import java.time.{Duration, Instant}
import java.util.UUID
import scala.collection.mutable.Set

object AuthorizationsImpl {
  def impl: Authorizations = new Authorizations {
    val authorizations: Set[Authorization] = Set()

    def create(clientId: client.Id,
               redirectionUri: Uri,
               scope: Option[client.Scope],
               state: Option[authorization.State],
               resourceOwner: ResourceOwner): Authorization = {
      val newAuthorization = authorization.Authorization(
        code = authorization.Code(UUID.randomUUID()),
        clientId = clientId,
        redirectionUri = redirectionUri,
        scope = scope,
        state = state,
        date = Instant.now(),
        validity = Duration.ofSeconds(60),
        redeemed = false,
        resourceOwner = resourceOwner
      )
      authorizations.addOne(newAuthorization)
      newAuthorization
    }

    def findByCode(code: authorization.Code): Option[Authorization] = authorizations.find(a => a.code == code)
  }
}