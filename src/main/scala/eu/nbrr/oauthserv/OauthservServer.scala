package eu.nbrr.oauthserv

import cats.effect.{ConcurrentEffect, Timer}
import eu.nbrr.oauthserv.impls.{AuthorizationsImpl, RegisteredClientsImpl, ResourceOwnersImpl, TokensImpl}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object OauthservServer {

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F]): Stream[F, Nothing] = {


      val authorizationsAlg = AuthorizationsImpl.impl
      val roAlg = ResourceOwnersImpl.impl
      val rcAlg = RegisteredClientsImpl.impl
      val tokensAlg = TokensImpl.impl

      // Combine Service Routes into an HttpApp.
      // Can also be done via a Router if you
      // want to extract a segments not checked
      // in the underlying routes.
      val httpApp = OauthservRoutes.authorizationsRoutes[F](authorizationsAlg, roAlg, rcAlg, tokensAlg).orNotFound

      // With Middlewares in place
      val finalHttpApp = Logger.httpApp(true, true)(httpApp)
      for {
      exitCode <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
