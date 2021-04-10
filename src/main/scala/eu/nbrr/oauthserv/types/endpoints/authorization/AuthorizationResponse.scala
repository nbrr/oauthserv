package eu.nbrr.oauthserv.types.endpoints.authorization

import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.authorization.State
import eu.nbrr.oauthserv.types.client.{Id, Scope}
import org.http4s.Status.{BadRequest, Ok}
import org.http4s.headers.`Content-Type`
import org.http4s.{MediaType, QueryParamEncoder, Response, Uri}

sealed trait AuthorizationResponse {
  def response[F[_]](): Response[F]
}

case class AuthorizationCodeGrantResponseSuccess(clientId: Id,
                                                 redirectionUri: Uri,
                                                 maybeState: Option[State],
                                                 maybeScope: Option[Scope]) extends AuthorizationResponse {
  override def response[F[_]](): Response[F] =
    Response[F](status = Ok)
      .withHeaders(`Content-Type`(MediaType.text.html))
      .withEntity(htmlForm("code", clientId, redirectionUri, maybeState, maybeScope))

  //  Response[F](status = Found).withHeaders(Location(
  //    authorization.redirectionUri
  //      .withQueryParam("code", authorization.code)
  //      .withOptionQueryParam("state", authorization.state)))
}

case class ImplicitGrantResponseSuccess(clientId: Id,
                                        redirectionUri: Uri,
                                        maybeState: Option[State],
                                        maybeScope: Option[Scope]) extends AuthorizationResponse {
  override def response[F[_]](): Response[F] =
    Response[F](status = Ok)
      .withHeaders(`Content-Type`(MediaType.text.html))
      .withEntity(htmlForm("token", clientId, redirectionUri, maybeState, maybeScope))
}


case class AuthorizationResponseError()
/*redirectionUri: Uri,
                                      error: ErrorType,
                                      description: Option[ErrorDescription],
                                      uri: Option[ErrorUri],
                                      state: Option[State])*/
  extends AuthorizationResponse {
  override def response[F[_]](): Response[F] =
    Response(status = BadRequest)


  /*Response[F](status = Found).withHeaders(Location(
      redirectionUri
        .withQueryParam("error", error)
        .withOptionQueryParam("state", state)
        .withQueryParam("error_description", description.toString) // FIXME deal with option
        .withQueryParam("error_uri", uri.toString)))*/
}

case class AuthorizationResponseTypeResponseError(responseType: String) extends AuthorizationResponse {
  override def response[F[_]](): Response[F] = Response(status = BadRequest)
  // TODO explain in response that the redirection URI is not correct
}

object authorizationQueryParamEncoders {
  implicit val authorizationStateQueryParamEncoder: QueryParamEncoder[State] =
    QueryParamEncoder[String].contramap(_.toString)

  implicit val authorizationCodeQueryParamEncoder: QueryParamEncoder[authorization.Code] =
    QueryParamEncoder[String].contramap(_.toString)

  implicit val AuthorizationResponseErrorTypeQueryParamEncoder: QueryParamEncoder[ErrorType] =
    QueryParamEncoder[String].contramap(_.toString)
}

object htmlForm {
  def apply(responseType: String,
            clientId: client.Id,
            redirectionUri: Uri,
            maybeState: Option[authorization.State],
            maybeScope: Option[client.Scope]): String = { // FIXME find some form builder?
    val maybeStateField = maybeState.map(state => "<input type='hidden' name='state' value='" + state.toString + "' />").getOrElse("")
    val maybeScopeField = maybeScope.map(_.get().map(scopeElt =>
      "<input type='checkbox' name='scope' id='" + scopeElt + "' value='" + scopeElt + "' />" +
        "<label for='" + scopeElt + "'>" + scopeElt + "</label>"
    ).mkString("")).getOrElse("")
    "<html>" +
      "<body>" + // FIXME why is it generating quotes here
      "<form action='/authentication?response_type=" + responseType.toString + "' method='post' accept-charset='utf-8'>" +
      "<input type='text' name='ro_id' id='ro_id' /><label for='ro_id'>username</label>" +
      "<input type='password' name='ro_secret' id='ro_secret' /><label for='ro_secret'>password</label>" +
      "<input type='hidden' name='client_id' value='" + clientId.toString + "' />" +
      "<input type='hidden' name='redirect_uri' value='" + redirectionUri.toString + "' />" +
      maybeStateField +
      maybeScopeField +
      "<input type='submit'/>" +
      "</form>" +
      "</body>" +
      "</html>"
  }
}