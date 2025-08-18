package bequiet.http.app.routes

import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.scalatags.*
import bequiet.util.http4s.*
import bequiet.services.{Songs, Charts}
import bequiet.domain.song.Song
import bequiet.http.app.templates
import org.typelevel.log4cats.Logger

final case class SongRoutes[F[_]: Concurrent: Logger](
    private val songs: Songs[F],
    private val charts: Charts[F]
) extends Http4sDsl[F]:
  private def findSong(idStr: String): OptionT[F, Song] =
    OptionT(idStr.toIntOption.flatTraverse(songs.find))

  private val songsRoute = HttpRoutes.of[F]:
    case GET -> Root / "songs" =>
      Ok(songs.all.compile.toList.map(templates.songs))

  private val songChartsRoute = HttpRoutes.of[F]:
    case GET -> Root / "song" / idStr / "charts" =>
      findSong(idStr)
        .semiflatMap(song =>
          Logger[F].info(song.show) *>
            charts
              .forSongId(song)
              .map(templates.charts(_, song))
        )
        .toResponse()

  val routes = songsRoute <+> songChartsRoute
