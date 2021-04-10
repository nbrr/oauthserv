package eu.nbrr.oauthserv.types.authorization

import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.endpoints.token.ErrorType
import org.http4s.Status.{BadRequest, Ok}
import org.http4s.headers.`Content-Type`
import org.http4s.{MediaType, Response, Uri}

sealed trait DispatchResponse {
  def response[F[_]](): Response[F]
}


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
