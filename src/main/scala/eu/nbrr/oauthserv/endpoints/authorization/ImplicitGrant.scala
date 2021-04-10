package eu.nbrr.oauthserv.endpoints.authorization

import eu.nbrr.oauthserv.traits.RegisteredClients
import eu.nbrr.oauthserv.types.authorization.State
import eu.nbrr.oauthserv.types.client.{Id, Scope}
import eu.nbrr.oauthserv.types.endpoints.authorization.{AuthorizationResponseError, _}
import org.http4s.Uri


object ImplicitGrant {
  def apply(clientId: Id,
            redirectionUri: Uri, // TODO can be optional for registered client
            maybeState: Option[State],
            maybeScope: Option[Scope])
           (implicit RC: RegisteredClients): AuthorizationResponse =
    RC.findById(clientId) match {
      case Some(client) =>
        if (client.redirectionUri == redirectionUri) {
          maybeScope match {
            case Some(scope) => // TODO spec says scope can be required or a default behaviour can be set
              if (client.scope.contains(scope)) {
                ImplicitGrantResponseSuccess(client.id, redirectionUri, maybeState, maybeScope)
              } else {
                AuthorizationResponseError() // scope doesn't match registered client's scope
              }
            case None => AuthorizationResponseError() // scope missing
          }
        } else {
          AuthorizationResponseError() // redirection URI must match that for the registered client
        }

      case None => AuthorizationResponseError() // client id non existant
    }
}