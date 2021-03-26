package eu.nbrr.oauthserv

import cats.effect.Sync
import cats.implicits._
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types.TokenResponseDecoders._
import eu.nbrr.oauthserv.types._
import io.circe.syntax.EncoderOps
import org.http4s.FormDataDecoder.{field, formEntityDecoder}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.{FormDataDecoder, _}

import java.time.Instant

case class GrantType(value: String)

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
    implicit val authorizationCodeQueryParamDecoder: QueryParamDecoder[AuthorizationCode] =
      QueryParamDecoder[String].map(AuthorizationCode(_))
    implicit val grantTypeQueryParamDecoder: QueryParamDecoder[GrantType] =
      QueryParamDecoder[String].map(GrantType)

    implicit val authorizationCodeQueryParamEncoder: QueryParamEncoder[AuthorizationCode] =
      QueryParamEncoder[String].contramap(c => c.value.toString)

    object ResponseTypeQueryParamMatcher extends QueryParamDecoderMatcher[String]("response_type")
    object ClientIdQueryParamMatcher extends QueryParamDecoderMatcher[ClientId]("client_id")
    object RedirectionUriQueryParamMatcher extends QueryParamDecoderMatcher[Uri]("redirect_uri")
    object StateQueryParamMatcher extends QueryParamDecoderMatcher[AuthorizationState]("state")
    // TODO add scopes

    case class AuthenticationForm(roId: RoId, clientId: ClientId, redirectionUri: Uri, state: AuthorizationState)
    implicit val authenticationMapper: FormDataDecoder[AuthenticationForm] =
      (field[RoId]("ro_id"),
        field[ClientId]("client_id"),
        field[Uri]("redirect_uri"),
        field[AuthorizationState]("state")).mapN(AuthenticationForm.apply)

    case class TokenRequest(grantType: GrantType, code: AuthorizationCode, redirectUri: Uri, clientId: ClientId)
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
          val rosInputs = RO.getAll().map(ro =>
            s"<input type='radio' name='ro_id' value='${ro.id.value.toString}' id='${ro.id.value.toString}'/>" +
              s"<label for='${ro.id.value.toString}'>${ro.id.value.toString}</label>").mkString("<br/>")
          val form = {
            "<html>" +
              "<body>" + // FIXME why is it generating quotes here
              "<form action='/authentication' method='post' accept-charset='utf-8'>" +
              rosInputs +
              "<input type='hidden' name='client_id' value='" + clientIdParameter.value.toString + "' />" + // TODO implement toString directly for the types
              "<input type='hidden' name='redirect_uri' value='" + redirectionUri.toString + "' />" +
              "<input type='hidden' name='state' value='" + state.value.toString + "' />" +
              "<input type='submit'/>" +
              "</form>" +
              "</body>" +
              "</html>"
          }
          Ok(form, `Content-Type`(MediaType.text.html)) // TODO question: where in the doc can I find the Ok(...) signature?
        } else {
          BadRequest()
        }
      }
      case req@POST -> Root / "authentication" => {
        // TODO proper authentication check
        // TODO get all encoder/decoders somewhere it makes sense
        for {
          form <- req.as[AuthenticationForm]
          authorization = A.create(form.clientId, form.redirectionUri, List(), form.state, RO.findById(form.roId).get)
          resp <- Found()
        } yield resp.withHeaders(Location(authorization.redirectionUri
          .withQueryParam("code", authorization.code)
          .withQueryParam("state", authorization.state.value)))
      }
      case req@POST -> Root / "token" => {
        // FIXME write this in a cleaner manner. Use Either ?
        for {
          // FIXME invalid_scope error & scope check somewhere
          // TODO invalid_grant error: spec also mentions resource owner credentials might be wrong at this point, why ?
          tokenRequest <- req.as[TokenRequest] // TODO invalid_request should occur is there is a failure here
          tokenResponse <-
          if (tokenRequest.grantType.value == "authorization_code") {
            RC.findById(tokenRequest.clientId) match { // TODO client authentication for client
              case None => BadRequest(TokenResponseError(InvalidClient(), Some(ErrorDescription("client not found")), None).asJson)
              case Some(client) => {
                A.findByCode(tokenRequest.code) match {
                  case None => BadRequest(TokenResponseError(InvalidGrant(), Some(ErrorDescription("authorization code doesn't match the authorization request")), None).asJson)
                  case Some(authorization) =>
                    if (authorization.clientId != client.id) {
                      BadRequest(TokenResponseError(InvalidGrant(), Some(ErrorDescription("client doesn't match the authorization request")), None).asJson)
                    } else if (authorization.redirectionUri != tokenRequest.redirectUri) {
                      BadRequest(TokenResponseError(InvalidGrant(), Some(ErrorDescription("redirection uri doesn't match the authorization request")), None).asJson)
                    } else if (authorization.date.plus(authorization.validity).isAfter(Instant.now)) {
                      BadRequest(TokenResponseError(InvalidGrant(), Some(ErrorDescription("authorization request has expired")), None).asJson)
                    } else {
                      val token = T.create(authorization.scopes, true) // FIXME mark authorization grant as used
                      Ok(TokenResponseSuccess(
                        accessToken = token.accessToken,
                        //  tokenType = ,
                        expiresIn = Some(token.validity),
                        refreshToken = token.refreshToken,
                        scope = Some(token.scope)).asJson)
                    }
                }
              }
            }
          } else {
            BadRequest(TokenResponseError(UnsupportedGrantType(), None, None).asJson)
          }
        } yield tokenResponse
        // FIXME why slow to respond? esp on errors
      }
    }
  }
}
