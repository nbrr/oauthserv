package eu.nbrr.oauthserv.impls

import eu.nbrr.oauthserv.traits.ResourceOwners
import eu.nbrr.oauthserv.types.resource_owner.{ResourceOwner, RoId, RoName, RoSecret}

import scala.collection.mutable.Set

object ResourceOwnersImpl {
  def impl: ResourceOwners = new ResourceOwners {
    val resourceOwners: Set[ResourceOwner] = Set(
      ResourceOwner(
        id = RoId("alice"),
        name = RoName("Alice"),
        secret = RoSecret("Alice123")
      ),
      ResourceOwner(
        id = RoId("bob"),
        name = RoName("Bob"),
        secret = RoSecret("Bob123")
      ),
    )

    def getAll(): List[ResourceOwner] = resourceOwners.toList

    def findById(id: RoId): Option[ResourceOwner] = resourceOwners.find(ro => ro.id == id)

    def findAuthenticate(id: RoId, secret: RoSecret): Option[ResourceOwner] = findById(id).filter(ro => ro.secret == secret)
  }
}
