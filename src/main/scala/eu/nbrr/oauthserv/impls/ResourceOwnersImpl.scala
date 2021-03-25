package eu.nbrr.oauthserv.impls

import eu.nbrr.oauthserv.traits.ResourceOwners
import eu.nbrr.oauthserv.types.{ResourceOwner, RoId, RoName, RoSecret}

import java.util.UUID
import scala.collection.mutable.Set

object ResourceOwnersImpl {
  def impl: ResourceOwners = new ResourceOwners {
    val resourceOwners: Set[ResourceOwner] = Set(
      ResourceOwner(
        id = RoId(UUID.fromString("09db2078-e2f9-403e-9237-4115325584a0")),
        name = RoName("Alice"),
        secret = RoSecret("Alice123")
      ),
      ResourceOwner(
        id = RoId(UUID.fromString("40ac87bb-1b7a-4423-8fab-ba721984d64f")),
        name = RoName("Bob"),
        secret = RoSecret("Bob123")
      ),
    )

    def getAll(): List[ResourceOwner] = resourceOwners.toList
    def findById(id: RoId): Option[ResourceOwner] = resourceOwners.find(ro => ro.id == id)
  }
}
