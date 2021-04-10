package eu.nbrr.oauthserv.endpoints.authentication

import cats.effect.Sync
import cats.syntax.all._
import eu.nbrr.oauthserv.ParamDecoders._
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.endpoints.authentication._
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s.Request

object Authentication {
  def apply[F[_] : Sync](req: Request[F], responseType: String)(implicit A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): F[AuthenticationResponse] = {
    responseType match {
      case "code" => {
        for {
          form <- req.as[AuthenticationRequest]
          authenticationResponse = RO.findAuthenticate(form.roId, form.roSecret) match {
            case None =>
              AuthenticationResponseError( // incorrect resource owner credentials
                redirectionUri = form.redirectionUri,
                error = AccessDenied(),
                errorDescription = None, // TODO expand on error info
                errorUri = None,
                state = form.state)
            case Some(ro) => {
              val authorization = A.create(form.clientId, form.redirectionUri, form.scope, form.state, ro)
              AuthorizationCodeGrantResponseSuccess(authorization)
            }
          }
        } yield authenticationResponse
      }
      case "token" => {
        // FIXME the client authenticates to the authorization endpoint, prob need to keep track of this before issuing token?
        //     since there is a need to be careful with redirection URI validity
        for {
          form <- req.as[AuthenticationRequest]
          authenticationResponse = RO.findAuthenticate(form.roId, form.roSecret) match {
            case None =>
              AuthenticationResponseError( // incorrect resource owner credentials
                redirectionUri = form.redirectionUri,
                error = AccessDenied(),
                errorDescription = None, // TODO expand on error info
                errorUri = None,
                state = form.state)
            case Some(ro) => {
              val token = T.create(scope = form.scope, refresh = false)
              ImplicitGrantResponseSuccess(
                redirectionUri = form.redirectionUri,
                tokenData = token,
                maybeState = form.state)
            }
          }
        } yield authenticationResponse
      }
      case _ => (ResponseTypeResponseError(responseType): AuthenticationResponse).pure[F]
    }
  }
}
