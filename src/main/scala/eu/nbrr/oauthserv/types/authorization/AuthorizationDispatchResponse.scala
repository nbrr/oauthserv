package eu.nbrr.oauthserv.types.authorization

import eu.nbrr.oauthserv.types.token.TokenErrorType
import eu.nbrr.oauthserv.types.{ClientId, RoId, RoSecret, Scope}
import org.http4s.Status.Ok
import org.http4s.headers.`Content-Type`
import org.http4s.{MediaType, Response, Uri}

sealed trait AuthorizationDispatchResponse {
  def response[F[_]](): Response[F]
}

case class AuthorizationDispatchResponseSuccess(clientId: ClientId, redirectionUri: Uri, state: Option[AuthorizationState], scope: Option[Scope]) extends AuthorizationDispatchResponse {
  val htmlForm: String = { // FIXME find some form builder?
    val maybeStateField = state.map(state => "<input type='hidden' name='state' value='" + state.toString + "' />").getOrElse("")
    val maybeScopeField = scope.map(_.get().map(scopeElt =>
      "<input type='checkbox' name='scope' id='" + scopeElt + "' value='" + scopeElt + "' />" +
        "<label for='" + scopeElt + "'>" + scopeElt + "</label>"
    ).mkString("")).getOrElse("")
    "<html>" +
      "<body>" + // FIXME why is it generating quotes here
      "<form action='/authentication' method='post' accept-charset='utf-8'>" +
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

  override def response[F[_]](): Response[F] =
    Response[F](status = Ok)
      .withHeaders(`Content-Type`(MediaType.text.html))
      .withEntity(htmlForm)
}

case class AuthorizationDispatchResponseError(error: TokenErrorType) extends AuthorizationDispatchResponse {
  override def response[F[_]](): Response[F] = ???
}

case class AuthenticationForm(roId: RoId, roSecret: RoSecret, clientId: ClientId, redirectionUri: Uri, state: Option[AuthorizationState], scope: Option[Scope])
