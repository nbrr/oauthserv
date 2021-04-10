package eu.nbrr.oauthserv.types.authorization

import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.endpoints.token.ErrorType
import org.http4s.Status.{BadRequest, Ok}
import org.http4s.headers.`Content-Type`
import org.http4s.{MediaType, Response, Uri}

sealed trait DispatchResponse {
  def response[F[_]](): Response[F]
}

object htmlForm {
  def apply(responseType: ResponseType,
            clientId: client.Id,
            redirectionUri: Uri,
            state: Option[authorization.State],
            scope: Option[client.Scope]): String = { // FIXME find some form builder?
    val maybeStateField = state.map(state => "<input type='hidden' name='state' value='" + state.toString + "' />").getOrElse("")
    val maybeScopeField = scope.map(_.get().map(scopeElt =>
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

sealed trait AuthorizationCodeDispatchResponse extends DispatchResponse

case class AuthorizationCodeDispatchResponseSuccess(clientId: client.Id,
                                                    redirectionUri: Uri,
                                                    state: Option[authorization.State],
                                                    scope: Option[client.Scope])
  extends AuthorizationCodeDispatchResponse {
  override def response[F[_]](): Response[F] =
    Response[F](status = Ok)
      .withHeaders(`Content-Type`(MediaType.text.html))
      .withEntity(htmlForm(authorization.AuthorizationCode(), clientId, redirectionUri, state, scope))
}


sealed trait ImplicitDispatchResponse extends DispatchResponse

case class ImplicitDispatchResponseSuccess(clientId: client.Id,
                                           redirectionUri: Uri,
                                           state: Option[authorization.State],
                                           scope: Option[client.Scope])
  extends ImplicitDispatchResponse {
  override def response[F[_]](): Response[F] =
    Response[F](status = Ok)
      .withHeaders(`Content-Type`(MediaType.text.html))
      .withEntity(htmlForm(authorization.Token(), clientId, redirectionUri, state, scope))
}

case class DispatchResponseError(error: ErrorType) extends AuthorizationCodeDispatchResponse with ImplicitDispatchResponse { // FIXME poor subtyping?
  override def response[F[_]](): Response[F] = Response[F](status = BadRequest) // TODO expand on that
}
