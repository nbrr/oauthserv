package eu.nbrr.oauthserv.types.endpoints.token

import eu.nbrr.oauthserv.coders.ParamEncoders._
import eu.nbrr.oauthserv.types.token.Token
import io.circe.syntax.EncoderOps
import org.http4s.Status.{BadRequest, Ok}
import org.http4s.circe.jsonEncoder
import org.http4s.{Response, Uri}

sealed trait TokenResponse {
  def response[F[_]](): Response[F]
}

case class AuthorizationCodeGrantResponseSuccess(token: Token) extends TokenResponse {
  override def response[F[_]](): Response[F] =
    Response[F](status = Ok)
      .withEntity((this: TokenResponse).asJson)
}

case class ResourceOwnerPasswordCredentialsResponseSuccess(token: Token) extends TokenResponse {
  override def response[F[_]](): Response[F] =
    Response[F](status = Ok)
      .withEntity((this: TokenResponse).asJson)
}

case class TokenResponseError(error: TokenErrorType, description: Option[String], uri: Option[Uri]) extends TokenResponse {
  override def response[F[_]](): Response[F] =
    Response[F](status = BadRequest)
      .withEntity((this: TokenResponse).asJson)
}
