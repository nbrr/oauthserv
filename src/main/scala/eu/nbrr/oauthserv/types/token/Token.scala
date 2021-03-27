package eu.nbrr.oauthserv.types.token

import eu.nbrr.oauthserv.types.Scope

import java.time.{Duration, Instant}
import java.util.UUID

case class AccessToken(value: UUID) {
  override def toString: String = value.toString
}

case class RefreshToken(value: UUID) {
  override def toString: String = value.toString
}

case class TokenType(value: String) {
  override def toString: String = value
}

case class Token(
                  accessToken: AccessToken,
                  issueDate: Instant,
                  validity: Duration,
                  refreshToken: Option[RefreshToken],
                  scope: List[Scope]
                )
