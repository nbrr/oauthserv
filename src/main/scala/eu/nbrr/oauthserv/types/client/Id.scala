package eu.nbrr.oauthserv.types.client

import java.util.UUID

case class Id(value: UUID) {
  override def toString: String = value.toString
}

object Id {
  def apply(s: String): Id = fromString(s)

  def fromString(s: String) = Id(UUID.fromString(s))
}
