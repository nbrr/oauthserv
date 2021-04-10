package eu.nbrr.oauthserv.types

import org.http4s.Uri

case class RegisteredClient(
                             id: client.Id,
                             _type: client.Type,
                             secret: client.Secret,
                             scope: client.Scope,
                             redirectionUri: Uri)
