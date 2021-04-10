package eu.nbrr.oauthserv.types.token

import java.util.UUID

case class RefreshToken(value: UUID) {
  override def toString: String = value.toString
}
