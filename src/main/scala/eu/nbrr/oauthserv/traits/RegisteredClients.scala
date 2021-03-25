package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types.{ClientId, RegisteredClient}

trait RegisteredClients {
  def findById(id: ClientId): Option[RegisteredClient]
}
