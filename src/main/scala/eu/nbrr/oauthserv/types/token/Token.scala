package eu.nbrr.oauthserv.types.token

import eu.nbrr.oauthserv.types.{client, token}

import java.time.{Duration, Instant}

case class Token(
                  accessToken: token.AccessToken,
                  tokenType: String, // FIXME: type this
                  issueDate: Instant,
                  validity: Duration,
                  refreshToken: Option[token.RefreshToken],
                  scope: Option[client.Scope]
                )
