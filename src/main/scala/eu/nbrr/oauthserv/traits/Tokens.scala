package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types.Scope
import eu.nbrr.oauthserv.types.token.Token

trait Tokens {
  def create(scope: Option[Scope], refresh: Boolean): Token
}
