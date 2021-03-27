package eu.nbrr.oauthserv.impls

import eu.nbrr.oauthserv.traits.Authorizations
import eu.nbrr.oauthserv.types.authorization.{Authorization, AuthorizationCode, AuthorizationState}
import eu.nbrr.oauthserv.types.{authorization, _}
import org.http4s.Uri

import java.time.{Duration, Instant}
import java.util.UUID
import scala.collection.mutable.Set

object AuthorizationsImpl {
  def impl: Authorizations = new Authorizations {
    val authorizations: Set[Authorization] = Set()

    def create(clientId: ClientId, redirectionUri: Uri, scopes: List[Scope], state: AuthorizationState, resourceOwner: ResourceOwner): Authorization = {
      val newAuthorization = authorization.Authorization(
        code = AuthorizationCode(UUID.randomUUID()),
        clientId = clientId,
        redirectionUri = redirectionUri,
        scopes = scopes,
        state = state,
        date = Instant.now(),
        validity = Duration.ofSeconds(60),
        redeemed = false,
        resourceOwner = resourceOwner
      )
      authorizations.addOne(newAuthorization)
      newAuthorization
    }

    def findByCode(code: AuthorizationCode): Option[Authorization] = authorizations.find(a => a.code == code)
  }
}
