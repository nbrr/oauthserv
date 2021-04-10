package eu.nbrr.oauthserv.coders

import eu.nbrr.oauthserv.types.authorization.{Code, State}
import eu.nbrr.oauthserv.types.client.Scope
import eu.nbrr.oauthserv.types.endpoints.authentication.AuthenticationErrorType
import eu.nbrr.oauthserv.types.endpoints.token
import eu.nbrr.oauthserv.types.endpoints.token.TokenResponse
import eu.nbrr.oauthserv.types.token.{AccessToken, TokenType}
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.http4s.QueryParamEncoder

import java.time.Duration

object ParamEncoders {
  implicit val codeQueryParamEncoder: QueryParamEncoder[Code] =
    QueryParamEncoder[String].contramap(_.toString)
  implicit val accessTokenQueryParamEncoder: QueryParamEncoder[AccessToken] =
    QueryParamEncoder[String].contramap(_.toString)
  implicit val tokenTypeQueryParamEncoder: QueryParamEncoder[TokenType] =
    QueryParamEncoder[String].contramap(_.toString)
  implicit val validityQueryParamEncoder: QueryParamEncoder[Duration] =
    QueryParamEncoder[String].contramap(_.toString)
  implicit val scopeQueryParamEncoder: QueryParamEncoder[Scope] =
    QueryParamEncoder[String].contramap(_.toString)
  implicit val stateQueryParamEncoder: QueryParamEncoder[State] =
    QueryParamEncoder[String].contramap(_.toString)
  implicit val authenticationErrorTypeQueryParamEncoder: QueryParamEncoder[AuthenticationErrorType] =
    QueryParamEncoder[String].contramap(_.toString)

  // FIXME omit None values
  implicit val encodeTokenResponse: Encoder[TokenResponse] = Encoder.instance {
    case trs@token.AuthorizationCodeGrantResponseSuccess(_) => trs.asJson
    case tre@token.TokenResponseError(_, _, _) => tre.asJson
  }

  implicit val encodeTokenResponseSuccess: Encoder[token.AuthorizationCodeGrantResponseSuccess] =
    Encoder.forProduct4(
      "access_token",
      /*"token_type",*/
      "expires_in",
      "refresh_token",
      "scope")(trs =>
      (trs.token.accessToken.toString,
        /*,*/
        trs.token.validity.toString, // FIXME should be optional
        trs.token.refreshToken.map(_.toString),
        trs.token.scope.mkString(",") // FIXME should be optional
      )) // FIXME time string format

  implicit val encodeTokenResponseError: Encoder[token.TokenResponseError] =
    Encoder.forProduct3(
      "error",
      "error_description",
      "error_uri")(tre =>
      (tre.error.toString,
        tre.description.toString,
        tre.uri.toString)
    )
}
