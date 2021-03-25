package eu.nbrr.oauthserv.types

import java.time.{Duration, Instant}
import java.util.UUID

case class AccessToken(value: UUID)
case class RefreshToken(value: UUID)
case class TokenType(value: String)

case class Token(
                accessToken: AccessToken,
                issueDate: Instant,
                validity: Duration,
                refreshToken: Option[RefreshToken],
                scope: List[Scope]
                )

case class TokenResponse(
                           accessToken: AccessToken,
                          // tokenType: TokenType,
                           expiresIn : Option[Duration],
                           refreshToken: Option[RefreshToken],
                           scope: Option[List[Scope]])