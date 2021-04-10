package eu.nbrr.oauthserv

import cats.implicits.catsSyntaxTuple6Semigroupal
import eu.nbrr.oauthserv.types.endpoints.authentication.AuthenticationRequest
import eu.nbrr.oauthserv.types.{RoId, RoSecret, authorization, client}
import org.http4s.FormDataDecoder.{field, fieldOptional}
import org.http4s.dsl.io.{OptionalQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.{FormDataDecoder, QueryParamDecoder, Uri}

object ParamDecoders {
  implicit val clientIdQueryParamDecoder: QueryParamDecoder[client.Id] =
    QueryParamDecoder[String].map(client.Id(_))
  implicit val clientSecretQueryParamDecoder: QueryParamDecoder[client.Secret] =
    QueryParamDecoder[String].map(client.Secret)
  implicit val authorizationStateQueryParamDecoder: QueryParamDecoder[authorization.State] =
    QueryParamDecoder[String].map(authorization.State)
  implicit val ScopeTypeQueryParameterDecoder: QueryParamDecoder[client.Scope] =
    QueryParamDecoder[String].map(client.Scope(_))
  implicit val roIdQueryParamDecoder: QueryParamDecoder[RoId] =
    QueryParamDecoder[String].map(RoId)
  implicit val roSecretQueryParamDecoder: QueryParamDecoder[RoSecret] =
    QueryParamDecoder[String].map(RoSecret)
  implicit val authorizationCodeQueryParamDecoder: QueryParamDecoder[authorization.Code] =
    QueryParamDecoder[String].map(authorization.Code(_))
  implicit val grantTypeQueryParamDecoder: QueryParamDecoder[GrantType] =
    QueryParamDecoder[String].map(GrantType)

  object ResponseTypeQueryParamMatcher extends QueryParamDecoderMatcher[String]("response_type")

  object ClientIdQueryParamMatcher extends QueryParamDecoderMatcher[client.Id]("client_id")

  object RedirectionUriQueryParamMatcher extends QueryParamDecoderMatcher[Uri]("redirect_uri")

  object MaybeStateQueryParamMatcher extends OptionalQueryParamDecoderMatcher[authorization.State]("state")

  object MaybeScopeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[client.Scope]("scope") // FIXME check scopes have been properly considered

  implicit val authenticationMapper: FormDataDecoder[AuthenticationRequest] =
    (field[RoId]("ro_id"),
      field[RoSecret]("ro_secret"),
      field[client.Id]("client_id"),
      field[Uri]("redirect_uri"),
      fieldOptional[authorization.State]("state"),
      fieldOptional[client.Scope]("scope")).mapN(AuthenticationRequest)
  /*  implicit val tokenRequestMapper: FormDataDecoder[TokenRequest] =
      (field[GrantType]("grant_type"),
        field[authorization.Code]("code"),
        field[Uri]("redirect_uri"),
        field[client.Id]("client_id"),
        field[client.Secret]("client_secret")).mapN(TokenRequest)*/

}
