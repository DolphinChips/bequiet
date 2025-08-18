package bequiet.http.api.routes

import cats.syntax.all.*
import cats.effect.Async
import bequiet.services.*

def apiRoutes[F[_]: Async](songs: Songs[F]) =
  val healthRoutes = HealthRoutes[F].routes
  val songRoutes = SongRoutes[F](songs).routes
  Seq(healthRoutes, songRoutes).reduce(_ <+> _)
