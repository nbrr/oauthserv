package eu.nbrr.oauthserv.endpoints.authentication

import cats.effect.Sync
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.endpoints.authentication._

object ImplicitGrant {
  def apply
  (req: AuthenticationRequest)
  (implicit A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): AuthenticationResponse = {
    RO.findAuthenticate(req.roId, req.roSecret) match {
      case None =>
        AuthenticationResponseError( // incorrect resource owner credentials
          redirectionUri = req.redirectionUri,
          error = AccessDenied(),
          errorDescription = None, // TODO expand on error info
          errorUri = None,
          state = req.state)
      case Some(ro) => {
        val token = T.create(scope = req.scope, refresh = false)
        ImplicitGrantResponseSuccess(
          redirectionUri = req.redirectionUri,
          tokenData = token,
          maybeState = req.state)
      }
    }
  }
}
