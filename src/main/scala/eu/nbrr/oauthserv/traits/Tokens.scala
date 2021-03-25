package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types.{Scope, Token}

trait Tokens {
  def create(scope: List[Scope], refresh: Boolean): Token
}
