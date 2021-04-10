package eu.nbrr.oauthserv.grants.`implicit`

import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.authorization.{DispatchResponseError, ImplicitDispatchResponse, ImplicitDispatchResponseSuccess}
import eu.nbrr.oauthserv.types.endpoints.token.{InvalidClient, InvalidScope}
import org.http4s.Uri

object Dispatch {
  def apply(clientId: client.Id,
            redirectionUri: Uri,
            maybeState: Option[authorization.State],
            maybeScope: Option[client.Scope])
           (implicit A: Authorizations,
            RO: ResourceOwners,
            RC: RegisteredClients,
            T: Tokens): ImplicitDispatchResponse = {
    RC.findById(clientId) match {
      case Some(client) => {
        if (client.redirectionUri == redirectionUri) {
          maybeScope match {
            case Some(scope) => { // TODO spec says scope can be required or a default behaviour can be set
              if (client.scope.contains(scope)) {
                ImplicitDispatchResponseSuccess(client.id, redirectionUri, maybeState, maybeScope)
              } else {
                DispatchResponseError(InvalidScope()) // scope doesn't match registered client's scope
              }
            }
            case None => DispatchResponseError(InvalidScope()) // scope missing
          }
        } else {
          DispatchResponseError(InvalidClient()) // FIXME not clear what error is specified when the redirection URI don't match
        }
      }
      case None => DispatchResponseError(InvalidClient()) // client id non existant
    }
  }
}