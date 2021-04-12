package eu.nbrr.oauthserv.coders

import cats.implicits.{catsSyntaxTuple3Semigroupal, catsSyntaxTuple4Semigroupal, catsSyntaxTuple5Semigroupal, catsSyntaxTuple6Semigroupal, toTraverseOps}
import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.client.Scope
import eu.nbrr.oauthserv.types.endpoints.authentication.AuthenticationRequest
import eu.nbrr.oauthserv.types.endpoints.token._
import eu.nbrr.oauthserv.types.resource_owner.{RoId, RoSecret}
import org.http4s.FormDataDecoder.{FormData, Result, field, fieldOptional}
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
    QueryParamDecoder[String].map(GrantType.fromString(_))

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

  implicit val AuthorizationCodeTokenRequestMapper: FormDataDecoder[AuthorizationCodeTokenRequest] =
    (field[authorization.Code]("code"),
      field[Uri]("redirect_uri"),
      field[client.Id]("client_id"),
      field[client.Secret]("client_secret")).mapN(AuthorizationCodeTokenRequest)


  implicit val ResourceOwnerPasswordCredentialsTokenRequestMapper: FormDataDecoder[ResourceOwnerPasswordCredentialsTokenRequest] =
    (field[RoId]("username"),
      field[RoSecret]("password"),
      fieldOptional[Scope]("Scope"),
      field[client.Id]("client_id"),
      field[client.Secret]("client_secret")).mapN(ResourceOwnerPasswordCredentialsTokenRequest)

  implicit val ClientCredentialsTokenRequestMapper: FormDataDecoder[ClientCredentialsTokenRequest] =
    (fieldOptional[Scope]("Scope"),
      field[client.Id]("client_id"),
      field[client.Secret]("client_secret")).mapN(ClientCredentialsTokenRequest)


  implicit val dummyTokenRequestMapper: FormDataDecoder[Option[Either[String, TokenRequest]]] = FormDataDecoder.apply { data =>
    val f: FormData => Option[Either[String, GrantTypeData]] = { data =>
      data.filter(_._2.nonEmpty)
        .get("grant_type")
        .flatMap(_.headOption)
        .map(GrantTypeData.eitherFromStringAndData(_, data))
    }
    val g: GrantTypeData => Result[TokenRequest] = {
      case AuthorizationCodeData(data) => AuthorizationCodeTokenRequestMapper(data): Result[TokenRequest]
      case ResourceOwnerPasswordCredentialsData(data) => ResourceOwnerPasswordCredentialsTokenRequestMapper(data): Result[TokenRequest]
      case ClientCredentialsData(data) => ClientCredentialsTokenRequestMapper(data): Result[TokenRequest]
    }

    f(data).traverse(t => t.traverse(g(_)))
  }
}
