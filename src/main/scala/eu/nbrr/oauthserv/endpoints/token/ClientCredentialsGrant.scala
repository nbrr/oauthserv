package eu.nbrr.oauthserv.endpoints.token

import eu.nbrr.oauthserv.traits.{RegisteredClients, Tokens}
import eu.nbrr.oauthserv.types.endpoints.token._

object ClientCredentialsGrant {
  def apply
  (req: ClientCredentialsTokenRequest)
  (implicit RC: RegisteredClients, T: Tokens): TokenResponse =
    RC.findById(req.clientId) match {
      case None => TokenResponseError(InvalidClient(), Some("client not found"), None)
      case Some(client) =>
        if (client.secret == req.clientSecret) {
          val token = T.create(req.scope, true) // FIXME check scope
          ResourceOwnerPasswordCredentialsResponseSuccess(token)
        } else {
          TokenResponseError(InvalidClient(), Some("authentication failed"), None) // TODO 401
        }
    }
}
