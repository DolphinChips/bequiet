package bequiet.http.app.routes

import cats.*
import cats.syntax.all.*
import cats.effect.Concurrent
import org.http4s.HttpRoutes
import bequiet.services.{Players, Scores, Songs, Charts}
import org.typelevel.log4cats.Logger

def appRoutes[F[_]: Concurrent: Logger](
    players: Players[F],
    scores: Scores[F],
    songs: Songs[F],
    charts: Charts[F]
): HttpRoutes[F] =
  val hello = HelloRoutes[F].routes
  val player = PlayerRoutes[F](players, scores).routes
  val song = SongRoutes[F](songs, charts).routes
  Seq(hello, player, song).reduce(_ <+> _)
