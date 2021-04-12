package eu.nbrr.oauthserv.endpoints.token


import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, Tokens}
import eu.nbrr.oauthserv.types.endpoints.token._

import java.time.Instant

object AuthorizationCodeGrant {
  def apply
  (req: AuthorizationCodeTokenRequest)
  (implicit A: Authorizations, RC: RegisteredClients, T: Tokens): TokenResponse =
    RC.findById(req.clientId) match { // TODO client authentication for client
      case None => TokenResponseError(InvalidClient(), Some("client not found"), None)
      case Some(client) =>
        if (client.secret == req.clientSecret) {
          A.findByCode(req.code) match {
            case None => TokenResponseError(InvalidGrant(), Some("authorization code doesn't match the authorization request"), None)
            case Some(authorization) =>
              if (authorization.clientId != client.id) {
                TokenResponseError(InvalidGrant(), Some("client doesn't match the authorization request"), None)
              } else if (authorization.redirectionUri != req.redirectUri) {
                TokenResponseError(InvalidGrant(), Some("redirection uri doesn't match the authorization request"), None)
              } else if (authorization.date.plus(authorization.validity).isBefore(Instant.now)) {
                TokenResponseError(InvalidGrant(), Some("authorization request has expired"), None)
              } else {
                val token = T.create(authorization.scope, true) // FIXME mark authorization grant as used
                AuthorizationCodeGrantResponseSuccess(token)
              }
          }
        } else {
          TokenResponseError(InvalidClient(), Some("authentication failed"), None) // TODO 401
        }
    }
}
