package eu.nbrr.oauthserv.types.authorization

import org.http4s.Status.Found
import org.http4s.headers.Location
import org.http4s.{QueryParamEncoder, Response, Uri}


// FIXME unnecessary refactor due to case class name conflict with TokenResponse for errors ?

import eu.nbrr.oauthserv.types.authorization.authorizationQueryParamEncoders._

sealed trait AuthorizationResponse {
  def response[F[_]](): Response[F]
}

case class AuthorizationResponseSuccess(authorization: Authorization) extends AuthorizationResponse {
  override def response[F[_]](): Response[F] = Response[F](status = Found).withHeaders(Location(
    authorization.redirectionUri
      .withQueryParam("code", authorization.code)
      .withOptionQueryParam("state", authorization.state)))
}

case class AuthorizationResponseError(redirectionUri: Uri, error: AuthorizationErrorType, description: Option[ErrorDescription], uri: Option[ErrorUri], state: Option[AuthorizationState]) extends AuthorizationResponse {
  override def response[F[_]](): Response[F] = Response[F](status = Found).withHeaders(Location(
    redirectionUri
      .withQueryParam("error", error)
      .withOptionQueryParam("state", state)
      .withQueryParam("error_description", description.toString) // FIXME deal with option
      .withQueryParam("error_uri", uri.toString)))
}

object authorizationQueryParamEncoders {
  implicit val authorizationStateQueryParamEncoder: QueryParamEncoder[AuthorizationState] =
    QueryParamEncoder[String].contramap(_.toString)

  implicit val authorizationCodeQueryParamEncoder: QueryParamEncoder[AuthorizationCode] =
    QueryParamEncoder[String].contramap(_.toString)

  implicit val AuthorizationResponseErrorTypeQueryParamEncoder: QueryParamEncoder[AuthorizationErrorType] =
    QueryParamEncoder[String].contramap(_.toString)
}


