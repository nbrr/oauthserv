package eu.nbrr.oauthserv.types.endpoints.authentication

import eu.nbrr.oauthserv.ParamEncoders._
import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.authorization.Authorization
import eu.nbrr.oauthserv.types.token.Token
import org.http4s.Status.{BadRequest, Found}
import org.http4s.headers.Location
import org.http4s.{Response, Uri}

sealed trait AuthenticationResponse {
  def response[F[_]](): Response[F]
}

case class AuthorizationCodeGrantResponseSuccess(authorization: Authorization)
  extends AuthenticationResponse {

  override def response[F[_]](): Response[F] =
    Response(status = Found).withHeaders(Location(
      authorization.redirectionUri
        .withQueryParam("code", authorization.code)
        .withOptionQueryParam("state", authorization.state)
}

// TODO:
//  Developers should note that some user-agents do not support the
//  inclusion of a fragment component in the HTTP "Location" response
//  header field.  Such clients will require using other methods for
//  redirecting the client than a 3xx redirection response -- for
//  example, returning an HTML page that includes a 'continue' button
//  with an action linked to the redirection URI.

case class ImplicitGrantResponseSuccess(redirectionUri: Uri,
                                        tokenData: Token,
                                        maybeState: Option[authorization.State])
  extends AuthenticationResponse {

  override def response[F[_]](): Response[F] =
    Response(status = Found)
      .withHeaders(Location(
        redirectionUri
          .withQueryParam("access_token", tokenData.accessToken)
          .withQueryParam("token_type", tokenData.tokenType)
          .withQueryParam("expires_in", tokenData.validity)
          .withOptionQueryParam("scope", tokenData.scope)
          .withOptionQueryParam("state", maybeState)))
}

case class AuthenticationResponseError(redirectionUri: Uri,
                                       error: AuthenticationErrorType,
                                       errorDescription: Option[String], // type this?
                                       errorUri: Option[Uri],
                                       state: Option[authorization.State]
                                      )
  extends AuthenticationResponse {

  override def response[F[_]](): Response[F] =
    Response(status = Found).withHeaders(Location(
      redirectionUri
        .withQueryParam("error", error)
        .withOptionQueryParam("error_description", errorDescription) // FIXME deal with option
        .withOptionQueryParam("error_uri", errorUri)
        .withOptionQueryParam("state", state)))
}

case class AuthenticationRedirectionUriResponseError(faultyRedirectionUri: Uri) extends AuthenticationResponse {
  override def response[F[_]](): Response[F] = Response(status = BadRequest)
  // TODO explain in response that the redirection URI is not correct
}

case class AuthenticationResponseTypeResponseError(responseType: String) extends AuthenticationResponse {
  override def response[F[_]](): Response[F] = Response(status = BadRequest)
  // TODO explain in response that the response type is not correct
}