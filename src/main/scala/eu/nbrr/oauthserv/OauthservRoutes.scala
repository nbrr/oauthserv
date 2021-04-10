package eu.nbrr.oauthserv

import cats.effect.Sync
import cats.syntax.all._
import eu.nbrr.oauthserv.coders.ParamDecoders._
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.AuthorizationCode
import eu.nbrr.oauthserv.types.endpoints._
import eu.nbrr.oauthserv.types.endpoints.authentication.{AuthenticationRequest, _}
import eu.nbrr.oauthserv.types.endpoints.authorization.AuthorizationResponseTypeResponseError
import eu.nbrr.oauthserv.types.endpoints.token.{TokenRequest, TokenResponseError}
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s._
import org.http4s.dsl.Http4sDsl

// FIXME force https
object OauthservRoutes {
  def authorizationsRoutes[F[_] : Sync](A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): HttpRoutes[F] = {
    implicit val A_ = A
    implicit val RO_ = RO
    implicit val RC_ = RC
    implicit val T_ = T
    val dsl = new Http4sDsl[F] {}
    import dsl._

    // HELP question: where in the doc can I find the Ok(...) signature? -> OkOps?
    HttpRoutes.of[F] {
      case GET -> Root / "authorization"
        :? ResponseTypeQueryParamMatcher(responseType)
        +& ClientIdQueryParamMatcher(clientIdParameter)
        +& RedirectionUriQueryParamMatcher(redirectionUri) // TODO can be optional for registered client
        +& MaybeStateQueryParamMatcher(maybeStateParameter)
        +& MaybeScopeQueryParamMatcher(maybeScopeParameter) => (responseType match {
        // FIXME rfc says nothing about the kind of error to be returned at this step?
        // pull token error type ?
        // > The authorization server validates the request to ensure that all
        // > required parameters are present and valid. If the request is valid,
        // > the authorization server authenticates the resource owner and obtains
        // > an authorization decision (by asking the resource owner or by
        // > establishing approval via other means).

        case "code" => endpoints.authorization.AuthorizationCodeGrant(clientIdParameter, redirectionUri, maybeStateParameter, maybeScopeParameter)
        case "token" => endpoints.authorization.ImplicitGrant(clientIdParameter, redirectionUri, maybeStateParameter, maybeScopeParameter)
        case _ => AuthorizationResponseTypeResponseError(responseType)
      }).response[F]().pure[F]
      case req@POST -> Root / "authentication" :? ResponseTypeQueryParamMatcher(responseType) => (responseType match {
        case "code" => for {
          form <- req.as[AuthenticationRequest]
          authenticationResponse = endpoints.authentication.AuthorizationCodeGrant(form): AuthenticationResponse
        } yield authenticationResponse
        case "token" => for {
          form <- req.as[AuthenticationRequest]
          authenticationResponse = endpoints.authentication.ImplicitGrant(form): AuthenticationResponse
        } yield authenticationResponse
        case _ => (AuthenticationResponseTypeResponseError(responseType): AuthenticationResponse).pure[F]
      }).map(_.response[F]())
      case req@POST -> Root / "token" => {
        // FIXME write this in a cleaner manner. Use Either ?
        // TODO proper client authentication
        // TOOO some extent of brute-force protection
        // TODO http basic authentication scheme
        for {
          // FIXME invalid_scope error & scope check somewhere
          // TODO invalid_grant error: spec also mentions resource owner credentials might be wrong at this point, why ?
          tokenRequest <- req.as[TokenRequest] // TODO invalid_request should occur if there is a failure here
          tokenResponse =
            if (tokenRequest.grantType == AuthorizationCode()) {
              endpoints.token.AuthorizationCodeGrant(tokenRequest)(A, RC, T)
            } else {
              TokenResponseError(token.InvalidGrant(), None, None)
            }
        } yield tokenResponse.response[F]()
        // HELP why slow to respond? esp on errors -> shouldn't infinite loop have been reported somehow?
      }
    }
  }
}
