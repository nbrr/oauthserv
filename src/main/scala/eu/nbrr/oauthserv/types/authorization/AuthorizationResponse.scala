package eu.nbrr.oauthserv.types.authorization

import org.http4s.Status.Found
import org.http4s.headers.Location
import org.http4s.{QueryParamEncoder, Response, Uri}

// FIXME unnecessary refactor due to case class name conflict with TokenResponse for errors ?


object authorizationQueryParamEncoders {
  implicit val authorizationStateQueryParamEncoder: QueryParamEncoder[AuthorizationState] =
    QueryParamEncoder[String].contramap(_.toString)

  implicit val authorizationCodeQueryParamEncoder: QueryParamEncoder[AuthorizationCode] =
    QueryParamEncoder[String].contramap(_.toString)

  implicit val AuthorizationResponseErrorTypeQueryParamEncoder: QueryParamEncoder[AuthorizationResponseErrorType] =
    QueryParamEncoder[String].contramap(_.toString)
}

import eu.nbrr.oauthserv.types.authorization.authorizationQueryParamEncoders._

sealed trait AuthorizationResponseErrorType

final case class InvalidRequest() extends AuthorizationResponseErrorType {
  override def toString: String = "invalid_request"
}

final case class UnauthorizedClient() extends AuthorizationResponseErrorType {
  override def toString: String = "unauthorized_client"
}

final case class AccessDenied() extends AuthorizationResponseErrorType {
  override def toString: String = "access_denied"
}

final case class UnsupportedResponseType() extends AuthorizationResponseErrorType {
  override def toString: String = "unsupported_response_type"
}

final case class InvalidScope() extends AuthorizationResponseErrorType {
  override def toString: String = "invalid_scope"
}

final case class ServerError() extends AuthorizationResponseErrorType {
  override def toString: String = "server_error"
}


final case class TemporarilyUnavailable() extends AuthorizationResponseErrorType {
  override def toString: String = "temporarily_unavailable"
}

sealed trait AuthorizationResponse[F[_]] {
  def response(): Response[F]
}

case class AuthorizationResponseSuccess[F[_]](authorization: Authorization) extends AuthorizationResponse[F] {
  override def response(): Response[F] = Response[F](status = Found).withHeaders(Location(
    authorization.redirectionUri
      .withQueryParam("code", authorization.code)
      .withQueryParam("state", authorization.state)))
}

case class AuthorizationResponseError[F[_]](redirectionUri: Uri, error: AuthorizationResponseErrorType, description: Option[ErrorDescription], uri: Option[ErrorUri], state: AuthorizationState) extends AuthorizationResponse[F] {
  override def response(): Response[F] = Response[F](status = Found).withHeaders(Location(
    redirectionUri
      .withQueryParam("error", error)
      .withQueryParam("state", state)
      .withQueryParam("error_description", description.toString) // FIXME deal with option
      .withQueryParam("error_uri", uri.toString)))
}

case class ErrorDescription(value: String) {
  override def toString: String = value
}

case class ErrorUri(value: Uri) {
  override def toString: String = value.toString
}

