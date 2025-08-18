package bequiet.http.api.routes

import cats.syntax.all.*
import cats.effect.Async
import io.circe.generic.auto.*
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import bequiet.domain.song.Song
import bequiet.services.Songs

final class SongRoutes[F[_]: Async](private val songs: Songs[F]):
  val routes =
    val allSongsRoute = endpoint.get
      .in("songs")
      .in("all")
      .out(jsonBody[List[Song]])
      .serverLogic(_ => (songs.all.compile.toList.map(_.pure)))
    Http4sServerInterpreter[F]().toRoutes(allSongsRoute)
