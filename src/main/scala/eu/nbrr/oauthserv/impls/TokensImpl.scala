package eu.nbrr.oauthserv.impls

import eu.nbrr.oauthserv.traits.Tokens
import eu.nbrr.oauthserv.types.Scopes
import eu.nbrr.oauthserv.types.token.{AccessToken, RefreshToken, Token}

import java.time.{Duration, Instant}
import java.util.UUID
import scala.Option.when
import scala.collection.mutable.Set

object TokensImpl {
  def impl: Tokens = new Tokens {
    val tokens: Set[Token] = Set()

    def create(scope: Option[Scopes], refresh: Boolean): Token = {
      val token = Token(
        accessToken = AccessToken(UUID.randomUUID),
        issueDate = Instant.now,
        validity = Duration.ofSeconds(3600),
        refreshToken = when(refresh)(RefreshToken(UUID.randomUUID)),
        scope = scope
      )
      tokens.addOne(token)
      token
    }
  }
}
