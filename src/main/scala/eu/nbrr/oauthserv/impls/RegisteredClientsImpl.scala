package eu.nbrr.oauthserv.impls

import eu.nbrr.oauthserv.traits.RegisteredClients
import eu.nbrr.oauthserv.types._
import org.http4s.implicits.http4sLiteralsSyntax

import java.util.UUID
import scala.collection.mutable.Set

object RegisteredClientsImpl {
  def impl: RegisteredClients = new RegisteredClients {
    val registeredClients: Set[RegisteredClient] = Set(
      RegisteredClient(
        id = ClientId(UUID.fromString("123e4567-e89b-12d3-a456-556642440000")),
        _type = ConfidentialClient(),
        secret = ClientSecret("client123"),
        scopes = List(Scope("foo"), Scope("bar")),
        redirectionUri = uri"https://example.com"
      )
    )

    def findById(id: ClientId): Option[RegisteredClient] = registeredClients.find(rc => rc.id == id)
  }
}
