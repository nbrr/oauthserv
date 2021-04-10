package eu.nbrr.oauthserv.traits

import eu.nbrr.oauthserv.types.resource_owner.{ResourceOwner, RoId, RoSecret}

trait ResourceOwners {
  def getAll(): List[ResourceOwner]

  def findById(id: RoId): Option[ResourceOwner]

  def findAuthenticate(id: RoId, secret: RoSecret): Option[ResourceOwner]
}
