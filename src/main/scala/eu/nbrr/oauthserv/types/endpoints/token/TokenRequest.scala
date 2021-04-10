package eu.nbrr.oauthserv.types.endpoints.token

import eu.nbrr.oauthserv.types.authorization.Code
import eu.nbrr.oauthserv.types.{GrantType, client}
import org.http4s.Uri

case class TokenRequest(grantType: GrantType, code: Code, redirectUri: Uri, clientId: client.Id, clientSecret: client.Secret)
