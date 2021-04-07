package eu.nbrr.oauthserv

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import eu.nbrr.oauthserv.grants.AuthorizationCodeGrant
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.authorization._
import org.http4s.FormDataDecoder.{field, fieldOptional, formEntityDecoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.{FormDataDecoder, _}

// TODO put these somewhere it makes sense
case class GrantType(value: String)

case class AuthenticationForm(roId: RoId, roSecret: RoSecret, clientId: ClientId, redirectionUri: Uri, state: Option[AuthorizationState], scope: Option[Scope])

case class TokenRequest(grantType: GrantType, code: AuthorizationCode, redirectUri: Uri, clientId: ClientId, clientSecret: ClientSecret)

// FIXME force https
object OauthservRoutes {
  def authorizationsRoutes[F[_] : Sync](A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): HttpRoutes[F] = {
    val _ = A
    val dsl = new Http4sDsl[F] {}
    import dsl._

    // HELP this probably can be improved ?
    implicit val clientIdQueryParamDecoder: QueryParamDecoder[ClientId] =
      QueryParamDecoder[String].map(ClientId(_))
    implicit val clientSecretQueryParamDecoder: QueryParamDecoder[ClientSecret] =
      QueryParamDecoder[String].map(ClientSecret)
    implicit val authorizationStateQueryParamDecoder: QueryParamDecoder[AuthorizationState] =
      QueryParamDecoder[String].map(AuthorizationState)
    implicit val ScopeTypeQueryParameterDecoder: QueryParamDecoder[Scope] =
      QueryParamDecoder[String].map(Scope(_))
    implicit val roIdQueryParamDecoder: QueryParamDecoder[RoId] =
      QueryParamDecoder[String].map(RoId)
    implicit val roSecretQueryParamDecoder: QueryParamDecoder[RoSecret] =
      QueryParamDecoder[String].map(RoSecret)
    implicit val authorizationCodeQueryParamDecoder: QueryParamDecoder[AuthorizationCode] =
      QueryParamDecoder[String].map(AuthorizationCode(_))
    implicit val grantTypeQueryParamDecoder: QueryParamDecoder[GrantType] =
      QueryParamDecoder[String].map(GrantType)

    object ResponseTypeQueryParamMatcher extends QueryParamDecoderMatcher[String]("response_type")
    object ClientIdQueryParamMatcher extends QueryParamDecoderMatcher[ClientId]("client_id")
    object RedirectionUriQueryParamMatcher extends QueryParamDecoderMatcher[Uri]("redirect_uri")
    object MaybeStateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[AuthorizationState]("state")
    object MaybeScopeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Scope]("scope") // FIXME check scopes have been properly considered

    implicit val authenticationMapper: FormDataDecoder[AuthenticationForm] =
      (field[RoId]("ro_id"),
        field[RoSecret]("ro_secret"),
        field[ClientId]("client_id"),
        field[Uri]("redirect_uri"),
        fieldOptional[AuthorizationState]("state"),
        fieldOptional[Scope]("scope")).mapN(AuthenticationForm.apply)


    implicit val tokenRequestMapper: FormDataDecoder[TokenRequest] =
      (field[GrantType]("grant_type"),
        field[AuthorizationCode]("code"),
        field[Uri]("redirect_uri"),
        field[ClientId]("client_id"),
        field[ClientSecret]("client_secret")).mapN(TokenRequest.apply)

    // HELP question: where in the doc can I find the Ok(...) signature? -> OkOps?
    HttpRoutes.of[F] {
      case GET -> Root / "authorization"
        :? ResponseTypeQueryParamMatcher(responseType)
        +& ClientIdQueryParamMatcher(clientIdParameter)
        +& RedirectionUriQueryParamMatcher(redirectionUri)
        +& MaybeStateQueryParamMatcher(maybeStateParameter)
        +& MaybeScopeQueryParamMatcher(maybeScopeParameter) => {
        // FIXME rfc says nothing about the kind of error to be returned at this step?
        // pull token error type ?
        // > The authorization server validates the request to ensure that all
        // > required parameters are present and valid. If the request is valid,
        // > the authorization server authenticates the resource owner and obtains
        // > an authorization decision (by asking the resource owner or by
        // > establishing approval via other means).
        val resp: AuthorizationDispatchResponse = // FIXME is that normal to pull error types from token?
        if (responseType == "code") { // TODO have these checks done in the param matchers?
          RC.findById(clientIdParameter) match {
            case Some(client) => {
              maybeScopeParameter match {
                case maybeScope@Some(scope) => { // TODO decide whether to make the scope optional
                  if (client.scope.contains(scope)) {
                    AuthorizationDispatchResponseSuccess(client.id, redirectionUri, maybeStateParameter, maybeScope)
                  } else {
                    AuthorizationDispatchResponseError(token.InvalidScope()) // scope doesn't match registered client's scope
                  }
                }
                case None => AuthorizationDispatchResponseError(token.InvalidScope()) // scope missing
              }
            }
            case None => AuthorizationDispatchResponseError(token.InvalidClient()) // client id non existant
          }
        } else {
          AuthorizationDispatchResponseError(token.UnsupportedGrantType()) // grant type not supported
        }
        Applicative[F].pure(resp.response[F]())
      }
      case req@POST -> Root / "authentication" => {
        // TODO proper authentication check
        // TODO present the scopes to the user.
        // TODO Properly deal with a request with no scope (fail or default)
        for {
          form <- req.as[AuthenticationForm]
          roAuthentication = RO.findAuthenticate(form.roId, form.roSecret)
          authenticationResult = roAuthentication match {
            case None => AuthorizationResponseError(error = AccessDenied(), redirectionUri = form.redirectionUri,
              description = None, uri = None, state = form.state)
            case Some(ro) => {
              val authorization = A.create(form.clientId, form.redirectionUri, form.scope, form.state, ro)
              AuthorizationResponseSuccess(authorization)
            }
          }
        } yield authenticationResult.response[F]()
      }
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
              token.TokenResponseError(token.UnsupportedGrantType(), None, None)
            }
        } yield tokenResult.response[F]()
        // HELP why slow to respond? esp on errors -> shouldn't infinite loop have been reported somehow?
      }
    }
  }
}
