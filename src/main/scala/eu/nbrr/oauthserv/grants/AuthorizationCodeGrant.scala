package eu.nbrr.oauthserv.grants

import cats.effect.Sync
import eu.nbrr.oauthserv.TokenRequest
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.token
import eu.nbrr.oauthserv.types.token.TokenResponseEncoders._
import eu.nbrr.oauthserv.types.token.{TokenResponseError, TokenResponseSuccess}
import io.circe.syntax.EncoderOps
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

import java.time.Instant


object AuthorizationCodeGrant {


  def apply[F[_] : Sync](tokenRequest: TokenRequest)(A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): F[Response[F]] = {
    val _ = RO
    val dsl = new Http4sDsl[F] {}
    import dsl._ // FIXME do I need to create this each time?
    RC.findById(tokenRequest.clientId) match { // TODO client authentication for client
      case None => BadRequest(TokenResponseError(token.InvalidClient(), Some(token.ErrorDescription("client not found")), None).asJson)
      case Some(client) => {
        A.findByCode(tokenRequest.code) match {
          case None => BadRequest(TokenResponseError(token.InvalidGrant(), Some(token.ErrorDescription("authorization code doesn't match the authorization request")), None).asJson)
          case Some(authorization) =>
            if (authorization.clientId != client.id) {
              BadRequest(TokenResponseError(token.InvalidGrant(), Some(token.ErrorDescription("client doesn't match the authorization request")), None).asJson)
            } else if (authorization.redirectionUri != tokenRequest.redirectUri) {
              BadRequest(TokenResponseError(token.InvalidGrant(), Some(token.ErrorDescription("redirection uri doesn't match the authorization request")), None).asJson)
            } else if (authorization.date.plus(authorization.validity).isBefore(Instant.now)) {
              BadRequest(TokenResponseError(token.InvalidGrant(), Some(token.ErrorDescription("authorization request has expired")), None).asJson)
            } else {
              val token = T.create(authorization.scopes, true) // FIXME mark authorization grant as used
              Ok(TokenResponseSuccess(
                accessToken = token.accessToken,
                //  tokenType = ,
                expiresIn = Some(token.validity),
                refreshToken = token.refreshToken,
                scope = Some(token.scope)).asJson)
            }
        }
      }
    }
  }
}
