package bequiet.http.app.routes

import cats.*
import cats.implicits.*
import cats.effect.Concurrent
import org.http4s.HttpRoutes
import bequiet.services.{Players, Scores}

def appRoutes[F[_]: Concurrent](
    players: Players[F],
    scores: Scores[F]
): HttpRoutes[F] =
  val hello = HelloRoutes[F].routes
  val player = PlayerRoutes[F](players, scores).routes
  Seq(hello, player).reduce(_ <+> _)
