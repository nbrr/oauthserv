package eu.nbrr.oauthserv

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import eu.nbrr.oauthserv.ParamDecoders._
import eu.nbrr.oauthserv.endpoints.authentication.Authentication
import eu.nbrr.oauthserv.grants.AuthorizationCodeGrant
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.authorization.{DispatchResponse, DispatchResponseError}
import eu.nbrr.oauthserv.types.endpoints.token.{TokenResponseError, UnsupportedGrantType}
import eu.nbrr.oauthserv.types.{client, endpoints, _}
import org.http4s._
import org.http4s.dsl.Http4sDsl


// TODO put these somewhere it makes sense
case class GrantType(value: String)

case class TokenRequest(grantType: GrantType, code: authorization.Code, redirectUri: Uri, clientId: client.Id, clientSecret: client.Secret)

// FIXME force https
object OauthservRoutes {
  def authorizationsRoutes[F[_] : Sync](implicit A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): HttpRoutes[F] = {
    val _ = A
    val dsl = new Http4sDsl[F] {}
    import dsl._



    // HELP question: where in the doc can I find the Ok(...) signature? -> OkOps?
    HttpRoutes.of[F] {
      case GET -> Root / "authorization"
        :? ResponseTypeQueryParamMatcher(responseType)
        +& ClientIdQueryParamMatcher(clientIdParameter)
        +& RedirectionUriQueryParamMatcher(redirectionUri) // TODO can be optional for registered client
        +& MaybeStateQueryParamMatcher(maybeStateParameter)
        +& MaybeScopeQueryParamMatcher(maybeScopeParameter) => {
        // FIXME rfc says nothing about the kind of error to be returned at this step?
        // pull token error type ?
        // > The authorization server validates the request to ensure that all
        // > required parameters are present and valid. If the request is valid,
        // > the authorization server authenticates the resource owner and obtains
        // > an authorization decision (by asking the resource owner or by
        // > establishing approval via other means).
        val resp: DispatchResponse = responseType match { // FIXME is that normal to pull error types from token?
          case "code" => // TODO refactor ? response type could come after the client & scope check ; logic not as smooth though
            grants.authorization_code.Dispatch(clientIdParameter, redirectionUri, maybeStateParameter, maybeScopeParameter)
          case "token" =>
            grants.`implicit`.Dispatch(clientIdParameter, redirectionUri, maybeStateParameter, maybeScopeParameter)
          case _ => // FIXME
            DispatchResponseError(UnsupportedGrantType()) // grant type not supported
        }
        Applicative[F].pure(resp.response[F]())
      }
      case req@POST -> Root / "authentication" :? ResponseTypeQueryParamMatcher(responseType) =>
        Authentication(req, responseType).map(_.response[F]())
      case req@POST -> Root / "token" => {
        // FIXME write this in a cleaner manner. Use Either ?
        // TODO proper client authentication
        // TOOO some extent of brute-force protection
        // TODO http basic authentication scheme
        for {
          // FIXME invalid_scope error & scope check somewhere
          // TODO invalid_grant error: spec also mentions resource owner credentials might be wrong at this point, why ?
          tokenRequest <- req.as[TokenRequest] // TODO invalid_request should occur is there is a failure here
          tokenResult =
            if (tokenRequest.grantType.value == "authorization_code") {
              AuthorizationCodeGrant(tokenRequest)(A, RO, RC, T)
            } else {
              TokenResponseError(endpoints.token.UnsupportedGrantType(), None, None)
            }
        } yield tokenResult.response[F]()
        // HELP why slow to respond? esp on errors -> shouldn't infinite loop have been reported somehow?
      }
    }
  }
}
