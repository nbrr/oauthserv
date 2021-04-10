package eu.nbrr.oauthserv.types.endpoints.authorization

import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.authorization.{Authorization, State}
import eu.nbrr.oauthserv.types.endpoints.authorization.authorizationQueryParamEncoders._
import org.http4s.Status.Found
import org.http4s.headers.Location
import org.http4s.{QueryParamEncoder, Response, Uri}

sealed trait AuthorizationResponse {
  def response[F[_]](): Response[F]
}

case class AuthorizationResponseSuccess(authorization: Authorization) extends AuthorizationResponse {
  override def response[F[_]](): Response[F] = Response[F](status = Found).withHeaders(Location(
    authorization.redirectionUri
      .withQueryParam("code", authorization.code)
      .withOptionQueryParam("state", authorization.state)))
}

case class AuthorizationResponseError(redirectionUri: Uri,
                                      error: ErrorType,
                                      description: Option[ErrorDescription],
                                      uri: Option[ErrorUri],
                                      state: Option[State])
  extends AuthorizationResponse {
  override def response[F[_]](): Response[F] = Response[F](status = Found).withHeaders(Location(
    redirectionUri
      .withQueryParam("error", error)
      .withOptionQueryParam("state", state)
      .withQueryParam("error_description", description.toString) // FIXME deal with option
      .withQueryParam("error_uri", uri.toString)))
}

object authorizationQueryParamEncoders {
  implicit val authorizationStateQueryParamEncoder: QueryParamEncoder[State] =
    QueryParamEncoder[String].contramap(_.toString)

  implicit val authorizationCodeQueryParamEncoder: QueryParamEncoder[authorization.Code] =
    QueryParamEncoder[String].contramap(_.toString)

  implicit val AuthorizationResponseErrorTypeQueryParamEncoder: QueryParamEncoder[ErrorType] =
    QueryParamEncoder[String].contramap(_.toString)
}
