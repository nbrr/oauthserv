package eu.nbrr.oauthserv.types.endpoints.token

import eu.nbrr.oauthserv.types.authorization.Code
import eu.nbrr.oauthserv.types.client
import eu.nbrr.oauthserv.types.client.Scope
import eu.nbrr.oauthserv.types.resource_owner.{RoId, RoSecret}
import org.http4s.Uri

sealed trait TokenRequest

case class AuthorizationCodeTokenRequest
(code: Code,
 redirectUri: Uri,
 clientId: client.Id,
 clientSecret: client.Secret)
  extends TokenRequest

case class ResourceOwnerPasswordCredentialsTokenRequest
(username: RoId,
 password: RoSecret,
 scope: Option[Scope],
 clientId: client.Id,
 clientSecret: client.Secret)
  extends TokenRequest

case class ClientCredentialsTokenRequest
(scope: Option[Scope],
 clientId: client.Id,
 clientSecret: client.Secret)
  extends TokenRequest
