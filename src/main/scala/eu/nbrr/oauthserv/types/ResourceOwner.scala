package eu.nbrr.oauthserv.types

import java.util.UUID

case class RoId(value: UUID)

case class RoName(value: String)

case class RoSecret(value: String)


case class ResourceOwner(id: RoId, name: RoName, secret: RoSecret)
