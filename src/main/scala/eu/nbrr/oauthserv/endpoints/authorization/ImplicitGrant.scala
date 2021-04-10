package eu.nbrr.oauthserv.endpoints.authorization

import cats.effect.Sync
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.authorization.State
import eu.nbrr.oauthserv.types.client.{Id, Scope}
import eu.nbrr.oauthserv.types.endpoints.authorization.{ AuthorizationResponseError, _}
import eu.nbrr.oauthserv.types.endpoints.token.InvalidScope
import org.http4s.{Request, Uri}


import cats.effect.Sync
import cats.syntax.all._
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.authorization.State
import eu.nbrr.oauthserv.types.client.{Id, Scope}
import eu.nbrr.oauthserv.types.endpoints.authentication.AuthenticationResponse
import eu.nbrr.oauthserv.types.endpoints.authorization._
import eu.nbrr.oauthserv.types.endpoints.token.InvalidScope
import org.http4s.{Request, Uri}


object ImplicitGrant {
  def apply(                         clientId: Id,
                         redirectionUri: Uri, // TODO can be optional for registered client
                         maybeState: Option[State],
                         maybeScope: Option[Scope])
                        (implicit A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): AuthorizationResponse =
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