package bequiet.cli.subcommands

import com.monovore.decline.*
import cats.*
import cats.effect.*
import cats.syntax.all.*
import bequiet.services.{Songs, Charts}
import bequiet.cli.codecs.*
import bequiet.domain.chart.{Playstyle, Difficulty}
import fs2.Stream
import org.typelevel.log4cats.Logger

case class ImportMdb(path: String)

object ImportMdb:
  val opts: Opts[ImportMdb] =
    Opts.subcommand("import-mdb", "Import music_data.bin file to database"):
      val path = Opts.argument[String](metavar = "path")
      path.map(ImportMdb.apply)

  def createSongsAndCharts[F[_]: Monad: Songs: Charts](t: (Song, Long)) =
    val (s, id) = t
    for
      () <- Songs[F].create(id.toInt, s.artist.trim, s.title.trim)
      diffs = s.levelsSP.toList.filter(_._2 == 12)
      () <- diffs.traverse_ { c =>
        val playstyle = Playstyle.SP
        val difficulty = Difficulty.fromString(c._1)
        Charts[F].create(id.toInt, playstyle, difficulty)
      }
    yield diffs.length

  def run[F[_]: Concurrent: Logger: Charts: Songs](
      file: Stream[F, Byte]
  ): F[ExitCode] =
    for
      count <- file
        .through(songPipe)
        .zipWithIndex
        .filter(
          _._1.levelsSP.toList.exists(_._2 == 12)
        )
        .evalMap(createSongsAndCharts)
        .compile
        .foldMonoid
      _ <- Logger[F].info(show"Added $count charts")
    yield ExitCode.Success
