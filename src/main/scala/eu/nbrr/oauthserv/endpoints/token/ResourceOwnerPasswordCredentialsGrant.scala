package eu.nbrr.oauthserv.endpoints.token

import eu.nbrr.oauthserv.traits.{ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.endpoints.token._

// FIXME consider client authentication
object ResourceOwnerPasswordCredentialsGrant {
  def apply
  (req: ResourceOwnerPasswordCredentialsTokenRequest)
  (implicit RO: ResourceOwners, T: Tokens): TokenResponse =
    RO.findAuthenticate(req.username, req.password) match {
      case None => TokenResponseError(InvalidGrant(), Some("user credentials incorrect"), None)
      case Some(_) => { // FIXME find out where in the spec is described the info about the ro that should be logged
        val token = T.create(req.scope, true) // FIXME mark authorization grant as used // FIXME check scope
        ResourceOwnerPasswordCredentialsResponseSuccess(token)
      }
    }
}
