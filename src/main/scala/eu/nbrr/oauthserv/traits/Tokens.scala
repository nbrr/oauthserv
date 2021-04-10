package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.token.Token

trait Tokens {
  def create(scope: Option[client.Scope], refresh: Boolean): Token
}
