package eu.nbrr.oauthserv.impls

import eu.nbrr.oauthserv.traits.RegisteredClients
import eu.nbrr.oauthserv.types._
import eu.nbrr.oauthserv.types.client.ConfidentialClient
import org.http4s.implicits.http4sLiteralsSyntax

import java.util.UUID
import scala.collection.mutable.Set

object RegisteredClientsImpl {
  def impl: RegisteredClients = new RegisteredClients {
    val registeredClients: Set[RegisteredClient] = Set(
      RegisteredClient(
        id = client.Id(UUID.fromString("123e4567-e89b-12d3-a456-556642440000")),
        _type = ConfidentialClient(),
        secret = client.Secret("client123"),
        scope = client.Scope("ceci cela"),
        redirectionUri = uri"https://example.com"
      )
    )

    def findById(id: client.Id): Option[RegisteredClient] = registeredClients.find(rc => rc.id == id)
  }
}
