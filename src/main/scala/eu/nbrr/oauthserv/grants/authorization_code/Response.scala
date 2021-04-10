package eu.nbrr.oauthserv.grants.authorization_code

import eu.nbrr.oauthserv.TokenRequest
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.endpoints
import eu.nbrr.oauthserv.types.endpoints.token._

import java.time.Instant

object Response {
  def apply(tokenRequest: TokenRequest)(implicit A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): TokenResponse = {
    val _ = RO
    RC.findById(tokenRequest.clientId) match {
      case None => TokenResponseError(InvalidClient(), Some(ErrorDescription("client not found")), None)
      case Some(client) => {
        if (client.secret == tokenRequest.clientSecret) { // TODO client authentication
          A.findByCode(tokenRequest.code) match {
            case None => TokenResponseError(InvalidGrant(), Some(endpoints.token.ErrorDescription("authorization code doesn't match the authorization request")), None)
            case Some(authorization) =>
              if (authorization.clientId != client.id) {
                TokenResponseError(endpoints.token.InvalidGrant(), Some(endpoints.token.ErrorDescription("client doesn't match the authorization request")), None)
              } else if (authorization.redirectionUri != tokenRequest.redirectUri) {
                TokenResponseError(endpoints.token.InvalidGrant(), Some(endpoints.token.ErrorDescription("redirection uri doesn't match the authorization request")), None)
              } else if (authorization.date.plus(authorization.validity).isBefore(Instant.now)) {
                TokenResponseError(endpoints.token.InvalidGrant(), Some(endpoints.token.ErrorDescription("authorization request has expired")), None)
              } else {
                val token = T.create(authorization.scope, true) // FIXME mark authorization grant as used
                TokenResponseSuccess(token)
              }
          }
        } else {
          TokenResponseError(endpoints.token.InvalidClient(), Some(endpoints.token.ErrorDescription("authentication failed")), None) // TODO 401
        }
      }
    }
  }
}
