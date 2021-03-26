package eu.nbrr.oauthserv.types

import java.util.UUID

case class RoId(value: UUID) {
  override def toString: String = value.toString
}
object RoId{
  def apply(s:String):RoId = fromString(s)
  def fromString(s: String) = RoId(UUID.fromString(s))
}
case class RoName(value: String)

case class RoSecret(value: String)


case class ResourceOwner(id: RoId, name: RoName, secret: RoSecret)
