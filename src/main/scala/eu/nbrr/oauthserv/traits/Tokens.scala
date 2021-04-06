package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types.Scopes
import eu.nbrr.oauthserv.types.token.Token

trait Tokens {
  def create(scope: Option[Scopes], refresh: Boolean): Token
}
