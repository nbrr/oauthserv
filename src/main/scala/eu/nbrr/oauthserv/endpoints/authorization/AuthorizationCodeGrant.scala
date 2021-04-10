package eu.nbrr.oauthserv.endpoints.authorization

import eu.nbrr.oauthserv.traits.RegisteredClients
import eu.nbrr.oauthserv.types.authorization.State
import eu.nbrr.oauthserv.types.client.{Id, Scope}
import eu.nbrr.oauthserv.types.endpoints.authorization.{AuthorizationCodeGrantResponseSuccess, AuthorizationResponse, AuthorizationResponseError}
import org.http4s.Uri

object AuthorizationCodeGrant {
  def apply
  (clientId: Id,
   redirectionUri: Uri, // TODO can be optional for registered client
   maybeState: Option[State],
   maybeScope: Option[Scope])
  (implicit RC: RegisteredClients): AuthorizationResponse =
    RC.findById(clientId) match {
      case Some(client) => maybeScope match {
        case Some(scope) => // TODO spec says scope can be required or a default behaviour can be set
          if (client.scope.contains(scope)) {
            AuthorizationCodeGrantResponseSuccess(client.id, redirectionUri, maybeState, maybeScope)
          } else { // FIXME what to do at this point when the scope is not valid?
            AuthorizationResponseError() // scope doesn't match registered client's scope
          }
        case None => // FIXME what to do at this point when the scope is not valid?
          AuthorizationResponseError()
      }
      case None => // FIXME what to do at this point when the client is not valid?
        AuthorizationResponseError()
    }
}
