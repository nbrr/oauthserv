package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types.{ResourceOwner, RoId}

trait ResourceOwners {
  def getAll(): List[ResourceOwner]
  def findById(id: RoId): Option[ResourceOwner]
}
