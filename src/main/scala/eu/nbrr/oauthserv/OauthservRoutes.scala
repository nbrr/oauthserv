package eu.nbrr.oauthserv

import cats.effect.Sync
import cats.implicits._
import eu.nbrr.oauthserv.traits.{Authorizations, RegisteredClients, ResourceOwners, Tokens}
import eu.nbrr.oauthserv.types._
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.http4s.FormDataDecoder.{field, formEntityDecoder}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.{FormDataDecoder, _}

import java.util.UUID

case class GrantType(value: String)

object OauthservRoutes {

  def jokeRoutes[F[_] : Sync](J: Jokes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        for {
          joke <- J.get
          resp <- Ok(joke)
        } yield resp
    }
  }

  def helloWorldRoutes[F[_] : Sync](H: HelloWorld[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        for {
          greeting <- H.hello(HelloWorld.Name(name))
          resp <- Ok(greeting)
        } yield resp
    }
  }


  def authorizationsRoutes[F[_] : Sync](A: Authorizations, RO: ResourceOwners, RC: RegisteredClients, T: Tokens): HttpRoutes[F] = {
    val _ = A
    val dsl = new Http4sDsl[F] {}
    import dsl._
    implicit val clientIdQueryParamDecoder: QueryParamDecoder[ClientId] =
      QueryParamDecoder[String].map(s => ClientId(UUID.fromString(s)))
    implicit val authorizationStateQueryParamDecoder: QueryParamDecoder[AuthorizationState] =
      QueryParamDecoder[String].map(AuthorizationState)
    implicit val roIdQueryParamDecoder: QueryParamDecoder[RoId] =
      QueryParamDecoder[String].map(s => RoId(UUID.fromString(s)))
    implicit val authorizationCodeQueryParamDecoder: QueryParamDecoder[AuthorizationCode] =
      QueryParamDecoder[String].map(s => AuthorizationCode(UUID.fromString(s)))
    implicit val grantTypeQueryParamDecoder: QueryParamDecoder[GrantType] =
      QueryParamDecoder[String].map(s => GrantType(s))


    implicit val authorizationCodeQueryParamEncoder: QueryParamEncoder[AuthorizationCode] =
      QueryParamEncoder[String].contramap(c => c.value.toString)


    object ResponseTypeQueryParamMatcher extends QueryParamDecoderMatcher[String]("response_type")
    object ClientIdQueryParamMatcher extends QueryParamDecoderMatcher[ClientId]("client_id")
    object RedirectionUriQueryParamMatcher extends QueryParamDecoderMatcher[Uri]("redirect_uri")
    object StateQueryParamMatcher extends QueryParamDecoderMatcher[AuthorizationState]("state")

    //object GrantTypeQueryParamMatcher extends QueryParamDecoderMatcher[String]("grant_type")
    //object AuthorizationCodeQueryParamMatcher extends QueryParamDecoderMatcher[AuthorizationCode]("code")
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
        // FIXME freshness check
        for {
          tokenRequest <- req.as[TokenRequest]
          resp <- if (tokenRequest.grantType.value == "authorization_code") {
            val t = for {
              client <- RC.findById(tokenRequest.clientId)
              authorization <- A.findByCode(tokenRequest.code)
              if authorization.clientId == client.id && authorization.redirectionUri == tokenRequest.redirectUri
              token = T.create(authorization.scopes, true)
              tokenResponse = TokenResponse(
                accessToken = token.accessToken,
                //  tokenType = ,
                expiresIn = Some(token.validity),
                refreshToken = token.refreshToken,
                scope = Some(token.scope))
            } yield tokenResponse
            t match {
              case Some(token) => Ok(token.asJson)
              case _ => BadRequest()
            }
          } else {
            BadRequest() // FIXME unsupported_grant_type error
          }
        } yield resp
      }
    }
  }

}
