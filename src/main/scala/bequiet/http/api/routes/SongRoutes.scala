package bequiet.http.api.routes

import cats.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.dsl.*
import bequiet.domain.song.Song
import bequiet.services.Songs
import bequiet.util.http4s.given

final class SongRoutes[F[_]: Monad] private (songs: Songs[F])
    extends Http4sDsl[F]:
  val http4sRoutes = HttpRoutes.of[F]:
    case GET -> Root / "songs" / "all" =>
      Ok(songs.all)

  val routes = http4sRoutes

object SongRoutes:
  def apply[F[_]: Monad](songs: Songs[F]) = new SongRoutes[F](songs)
