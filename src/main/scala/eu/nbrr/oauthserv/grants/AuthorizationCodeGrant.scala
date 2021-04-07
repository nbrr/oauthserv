package eu.nbrr.oauthserv.grants

import eu.nbrr.oauthserv.TokenRequest
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.token
import eu.nbrr.oauthserv.types.token.{TokenResponse, TokenResponseError, TokenResponseSuccess}

import java.time.Instant


object AuthorizationCodeGrant {
  def apply(tokenRequest: TokenRequest)(A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): TokenResponse = {
    val _ = RO
    RC.findById(tokenRequest.clientId) match { // TODO client authentication for client
      case None => TokenResponseError(token.InvalidClient(), Some(token.ErrorDescription("client not found")), None)
      case Some(client) => {
        if (client.secret == tokenRequest.clientSecret) {
          A.findByCode(tokenRequest.code) match {
            case None => TokenResponseError(token.InvalidGrant(), Some(token.ErrorDescription("authorization code doesn't match the authorization request")), None)
            case Some(authorization) =>
              if (authorization.clientId != client.id) {
                TokenResponseError(token.InvalidGrant(), Some(token.ErrorDescription("client doesn't match the authorization request")), None)
              } else if (authorization.redirectionUri != tokenRequest.redirectUri) {
                TokenResponseError(token.InvalidGrant(), Some(token.ErrorDescription("redirection uri doesn't match the authorization request")), None)
              } else if (authorization.date.plus(authorization.validity).isBefore(Instant.now)) {
                TokenResponseError(token.InvalidGrant(), Some(token.ErrorDescription("authorization request has expired")), None)
              } else {
                val token = T.create(authorization.scope, true) // FIXME mark authorization grant as used
                TokenResponseSuccess(token)
              }
          }
        } else {
          TokenResponseError(token.InvalidClient(), Some(token.ErrorDescription("authentication failed")), None) // TODO 401
        }
      }
    }
  }
}
