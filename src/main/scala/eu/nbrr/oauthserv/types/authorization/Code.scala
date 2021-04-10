package eu.nbrr.oauthserv.types.authorization

import java.util.UUID

case class Code(value: UUID) {
  override def toString: String = value.toString
}

object Code {
  def apply(s: String): Code = fromString(s)

  def fromString(s: String) = Code(UUID.fromString(s))
}
