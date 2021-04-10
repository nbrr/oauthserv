package eu.nbrr.oauthserv.types.token

import java.util.UUID

case class AccessToken(value: UUID) {
  override def toString: String = value.toString
}
