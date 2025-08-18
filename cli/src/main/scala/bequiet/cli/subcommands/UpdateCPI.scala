import bequiet.domain.chart.{Playstyle, Difficulty, ChartId}
import bequiet.domain.score.Lamp
import bequiet.services.{Songs, Charts, Ratings}
import cats.data.{NonEmptyList, OptionT}
import cats.derived.*
import cats.effect.*
import cats.*
import cats.syntax.all.*
import com.monovore.decline.*
import fs2.data.csv.*
import fs2.io.file.{Files, Path}
import fs2.{Pipe, Stream, text}
import org.typelevel.log4cats.Logger

case class CPI(ec: Float, nc: Float, hc: Float, exc: Float, fc: Float)
    derives Show

case class CPIRow private (title: String, difficulty: Difficulty, cpi: CPI)
    derives Show

object CPIRow:
  private val cpiTitleMapping = Map(
    "ACT0" -> "ACTØ",
    "ATHER" -> "ÆTHER",
    "Dans la nuit de l'eternite" -> "Dans la nuit de l'éternité",
    "Ignis†Irae" -> "Ignis†Iræ",
    "Macho Monky" -> "Mächö Mönky",
    "Ou Legends" -> "Ōu Legends",
    "Parvati" -> "Pārvatī",
    "RINNE" -> "RINИE",
    "Ubertreffen" -> "Übertreffen",
    "Xlo" -> "Xlø",
    "キャトられ恋はモ～モク" -> "キャトられ♥恋はモ～モク",
    "旋律のドグマ～Miserables～" -> "旋律のドグマ～Misérables～",
    "火影" -> "焱影",
    "表裏一体！？怪盗いいんちょの悩み" -> "表裏一体！？怪盗いいんちょの悩み♥"
  )

  private val decodeFloat: PartialFunction[String, DecoderResult[Float]] =
    case "-" => Right(Float.NaN)
    case s   => CellDecoder[Float].apply(s)

  given RowDecoder[CPIRow] = (row: Row) =>
    row.values match
      case NonEmptyList(titleWithDiff, List(ec, nc, hc, exc, fc)) =>
        val (title, diff) = titleWithDiff.takeRight(4) match
          case " [H]" => (titleWithDiff.dropRight(4), Difficulty.H)
          case " [A]" => (titleWithDiff.dropRight(4), Difficulty.A)
          case " [L]" => (titleWithDiff.dropRight(4), Difficulty.L)
          case _      => (titleWithDiff, Difficulty.A)
        for
          ec <- decodeFloat(ec)
          nc <- decodeFloat(nc)
          hc <- decodeFloat(hc)
          exc <- decodeFloat(exc)
          fc <- decodeFloat(fc)
        yield CPIRow(title.trim, diff, ec, nc, hc, exc, fc)
      case l if l.length != 6 => Left(new DecoderError("row is the wrong size"))
      case _                  => Left(new DecoderError("invalid CPI row"))

  def apply(
      title: String,
      difficulty: Difficulty,
      ec: Float,
      nc: Float,
      hc: Float,
      exc: Float,
      fc: Float
  ) =
    new CPIRow(
      title = cpiTitleMapping.getOrElse(title, title),
      difficulty = difficulty,
      cpi = CPI(
        ec = ec,
        nc = nc,
        hc = hc,
        exc = exc,
        fc = fc
      )
    )

case class LongCPI(chartId: ChartId, lamp: Lamp, value: Float)

def toLongCPI[F[_]]: Pipe[F, (ChartId, CPI), LongCPI] =
  _.flatMap { case (id, cpi) =>
    Stream(
      LongCPI(id, Lamp.EasyClear, cpi.ec),
      LongCPI(id, Lamp.NormalClear, cpi.nc),
      LongCPI(id, Lamp.HardClear, cpi.hc),
      LongCPI(id, Lamp.ExHardClear, cpi.exc),
      LongCPI(id, Lamp.FullCombo, cpi.fc)
    )
  }

case class UpdateCPI()

object UpdateCPI:
  val opts: Opts[UpdateCPI] =
    Opts.subcommand("update-cpi", "Update data from cpi.makecir.com")(
      Opts.apply(UpdateCPI())
    )

  private def findChart[F[_]: Monad: Songs: Charts](
      row: CPIRow
  ): OptionT[F, (ChartId, CPI)] =
    OptionT(Songs[F].find(row.title))
      .semiflatMap(song => Charts[F].forSongId(song.id))
      .subflatMap {
        _.find { chart =>
          chart.playstyle == Playstyle.SP && chart.difficulty == row.difficulty
        }.map(chart => (chart.id, row.cpi))
      }

  def run[F[_]: Concurrent: Logger: Files: Songs: Charts: Ratings]()
      : F[ExitCode] = for
    count <- Files[F]
      .readAll(Path("/home/insep/cpi.csv"))
      .through(text.utf8.decode)
      .through(decodeWithoutHeaders[CPIRow]('\t'))
      .filterNot(_.cpi.ec.isNaN)
      .evalMap(row => findChart(row).value)
      .collect { case Some(x) => x }
      .through(toLongCPI)
      .evalTap { longCPI =>
        Ratings[F].create(longCPI.chartId, longCPI.lamp, longCPI.value)
      }
      .compile
      .count
    _ <- Logger[F].info(show"Added CPI for $count charts")
  yield ExitCode.Success
