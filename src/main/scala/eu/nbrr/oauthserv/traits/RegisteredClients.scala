package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types._

trait RegisteredClients {
  def findById(id: client.Id): Option[RegisteredClient]
}
