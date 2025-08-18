package bequiet.services

import cats.effect.*
import bequiet.domain.chart.*
import bequiet.domain.song.SongId
import doobie.*
import doobie.implicits.*
import doobie.util.*

trait Charts[F[_]]:
  def create(
      songId: SongId,
      playstyle: Playstyle,
      difficulty: Difficulty
  ): F[Int]
  def all: fs2.Stream[F, Chart]
  def forSongId(songId: SongId): F[List[Chart]]
  def fromChartId(id: ChartId): F[Chart]
  def find(id: Int): F[Option[Chart]]

object Charts:
  def apply[F[_]](using charts: Charts[F]) = charts

final case class LiveCharts[F[_]: Concurrent](private val xa: Transactor[F])
    extends Charts[F]:
  def findChartQuery(chartId: Int) =
    sql"""
      select
        chart_id, playstyle, difficulty
      from chart
      where chart_id = $chartId
    """
      .query[Chart]

  override def create(
      songId: SongId,
      playstyle: Playstyle,
      difficulty: Difficulty
  ) =
    sql"""
      insert into chart
        ( song_id
        , playstyle
        , difficulty )
      values
        ( ${songId}
        , ${playstyle}
        , ${difficulty} )
    """.update
      .withUniqueGeneratedKeys[Int]("chart_id")
      .transact(xa)

  override def all =
    sql"""
      select
        chart_id, playstyle, difficulty
      from chart
    """
      .query[Chart]
      .stream
      .transact(xa)

  override def forSongId(songId: SongId) =
    sql"""
      select
        chart_id, playstyle, difficulty
      from chart
      where song_id = $songId
    """
      .query[Chart]
      .to[List]
      .transact(xa)

  override def fromChartId(id: ChartId) =
    findChartQuery(id).unique
      .transact(xa)

  override def find(id: Int) =
    findChartQuery(id).option
      .transact(xa)
