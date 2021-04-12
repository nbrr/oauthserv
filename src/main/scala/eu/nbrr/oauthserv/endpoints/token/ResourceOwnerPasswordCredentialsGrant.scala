package eu.nbrr.oauthserv.endpoints.token

import eu.nbrr.oauthserv.traits.{RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.endpoints.token._

object ResourceOwnerPasswordCredentialsGrant {
  def apply
  (req: ResourceOwnerPasswordCredentialsTokenRequest)
  (implicit RC: RegisteredClients, RO: ResourceOwners, T: Tokens): TokenResponse =
    RC.findById(req.clientId) match {
      case None => TokenResponseError(InvalidClient(), Some("client not found"), None)
      case Some(client) =>
        if (client.secret == req.clientSecret) {
          RO.findAuthenticate(req.username, req.password) match {
            case None => TokenResponseError(InvalidGrant(), Some("user credentials incorrect"), None)
            case Some(_) => { // FIXME find out where in the spec is described the info about the ro that should be logged
              val token = T.create(req.scope, true) // FIXME mark authorization grant as used // FIXME check scope
              ResourceOwnerPasswordCredentialsResponseSuccess(token)
            }
          }
        } else {
          TokenResponseError(InvalidClient(), Some("authentication failed"), None) // TODO 401
        }
    }
}
