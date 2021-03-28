package eu.nbrr.oauthserv

import cats.effect.Sync
import cats.implicits._
import eu.nbrr.oauthserv.grants.AuthorizationCodeGrant
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.authorization._
import eu.nbrr.oauthserv.types.token.TokenResponseEncoders._
import io.circe.syntax.EncoderOps
import org.http4s.FormDataDecoder.{field, formEntityDecoder}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.{FormDataDecoder, _}

// TODO put these somewhere it makes sense
case class GrantType(value: String)

case class AuthenticationForm(roId: RoId, roSecret: RoSecret, clientId: ClientId, redirectionUri: Uri, state: AuthorizationState)

case class TokenRequest(grantType: GrantType, code: AuthorizationCode, redirectUri: Uri, clientId: ClientId)

// FIXME force https
object OauthservRoutes {
  def authorizationsRoutes[F[_] : Sync](A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): HttpRoutes[F] = {
    val _ = A
    val dsl = new Http4sDsl[F] {}
    import dsl._

    // HELP this probably can be improved ?
    implicit val clientIdQueryParamDecoder: QueryParamDecoder[ClientId] =
      QueryParamDecoder[String].map(ClientId(_))
    implicit val authorizationStateQueryParamDecoder: QueryParamDecoder[AuthorizationState] =
      QueryParamDecoder[String].map(AuthorizationState)
    implicit val roIdQueryParamDecoder: QueryParamDecoder[RoId] =
      QueryParamDecoder[String].map(RoId(_))
    implicit val roSecretQueryParamDecoder: QueryParamDecoder[RoSecret] =
      QueryParamDecoder[String].map(RoSecret(_))
    implicit val authorizationCodeQueryParamDecoder: QueryParamDecoder[AuthorizationCode] =
      QueryParamDecoder[String].map(AuthorizationCode(_))
    implicit val grantTypeQueryParamDecoder: QueryParamDecoder[GrantType] =
      QueryParamDecoder[String].map(GrantType)

    object ResponseTypeQueryParamMatcher extends QueryParamDecoderMatcher[String]("response_type")
    object ClientIdQueryParamMatcher extends QueryParamDecoderMatcher[ClientId]("client_id")
    object RedirectionUriQueryParamMatcher extends QueryParamDecoderMatcher[Uri]("redirect_uri")
    object StateQueryParamMatcher extends QueryParamDecoderMatcher[AuthorizationState]("state")
    // TODO add scopes

    implicit val authenticationMapper: FormDataDecoder[AuthenticationForm] =
      (field[RoId]("ro_id"),
        field[RoSecret]("ro_secret"),
        field[ClientId]("client_id"),
        field[Uri]("redirect_uri"),
        field[AuthorizationState]("state")).mapN(AuthenticationForm.apply)


    implicit val tokenRequestMapper: FormDataDecoder[TokenRequest] =
      (field[GrantType]("grant_type"),
        field[AuthorizationCode]("code"),
        field[Uri]("redirect_uri"),
        field[ClientId]("client_id")).mapN(TokenRequest.apply)

    HttpRoutes.of[F] {
      case GET -> Root / "authorization"
        :? ResponseTypeQueryParamMatcher(responseType)
        +& ClientIdQueryParamMatcher(clientIdParameter)
        +& RedirectionUriQueryParamMatcher(redirectionUri)
        +& StateQueryParamMatcher(state) => {
        val client = RC.findById(clientIdParameter)
        if (responseType == "code" & client.isDefined) { // TODO have these checks done in the param matchers?
          // TODO find out some form builder tool
          val form = {
            "<html>" +
              "<body>" + // FIXME why is it generating quotes here
              "<form action='/authentication' method='post' accept-charset='utf-8'>" +
              "<input type='text' name='ro_id' id='ro_id' />" +
              "<input type='password' name='ro_secret' id='ro_secret' />" +
              "<input type='hidden' name='client_id' value='" + clientIdParameter.value.toString + "' />" + // TODO implement toString directly for the types
              "<input type='hidden' name='redirect_uri' value='" + redirectionUri.toString + "' />" +
              "<input type='hidden' name='state' value='" + state.value.toString + "' />" +
              "<input type='submit'/>" +
              "</form>" +
              "</body>" +
              "</html>"
          }
          Ok(form, `Content-Type`(MediaType.text.html)) // TODO question: where in the doc can I find the Ok(...) signature? -> OkOps?
        } else {
          BadRequest()
        }
      }
      case req@POST -> Root / "authentication" => {
        // TODO proper authentication check
        // TODO get all encoder/decoders somewhere it makes sense
        for {
          form <- req.as[AuthenticationForm]
          roAuthentication = RO.findAuthenticate(form.roId, form.roSecret)
          authenticationResult = roAuthentication match {
            case None => AuthorizationResponseError[F](redirectionUri = form.redirectionUri, error = AccessDenied(),
              description = None, uri = None, state = form.state)
            case Some(ro) => {
              val authorization = A.create(form.clientId, form.redirectionUri, List(), form.state, ro)
              AuthorizationResponseSuccess[F](authorization)
            }
          }
        } yield authenticationResult.response()
      }
      case req@POST -> Root / "token" => {
        // FIXME write this in a cleaner manner. Use Either ?
        for {
          // FIXME invalid_scope error & scope check somewhere
          // TODO invalid_grant error: spec also mentions resource owner credentials might be wrong at this point, why ?
          tokenRequest <- req.as[TokenRequest] // TODO invalid_request should occur is there is a failure here
          tokenResponse <-
            if (tokenRequest.grantType.value == "authorization_code") {
              AuthorizationCodeGrant[F](tokenRequest)(A, RO, RC, T)
            } else {
              BadRequest(token.TokenResponseError(token.UnsupportedGrantType(), None, None).asJson)
            }
        } yield tokenResponse
        // HELP why slow to respond? esp on errors -> shouldn't infinite loop have been reported somehow?
      }
    }
  }
}
