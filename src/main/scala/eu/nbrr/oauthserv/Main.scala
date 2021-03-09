package eu.nbrr.oauthserv

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    OauthservServer.stream[IO].compile.drain.as(ExitCode.Success)
}
