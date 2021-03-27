package eu.nbrr.oauthserv.types

case class RoId(value: String)

case class RoName(value: String)

case class RoSecret(value: String)


case class ResourceOwner(id: RoId, name: RoName, secret: RoSecret)
