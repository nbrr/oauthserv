package eu.nbrr.oauthserv.endpoints.authentication

import eu.nbrr.oauthserv.traits.{ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.endpoints.authentication._

object ImplicitGrant {
  def apply
  (req: AuthenticationRequest)
  (implicit RO: ResourceOwners, T: Tokens): AuthenticationResponse = {
    RO.findAuthenticate(req.roId, req.roSecret) match {
      case None =>
        AuthenticationResponseError( // incorrect resource owner credentials
          redirectionUri = req.redirectionUri,
          error = AccessDenied(),
          errorDescription = None, // TODO expand on error info
          errorUri = None,
          state = req.state)
      case Some(_) => {
        val token = T.create(scope = req.scope, refresh = false)
        ImplicitGrantResponseSuccess(
          redirectionUri = req.redirectionUri,
          tokenData = token,
          maybeState = req.state)
      }
    }
  }
}
