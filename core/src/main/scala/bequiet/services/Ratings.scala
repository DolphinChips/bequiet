package bequiet.services

import cats.effect.*
import cats.syntax.all.*
import bequiet.domain.score.Lamp
import bequiet.domain.chart.ChartId
// import bequiet.domain.rating.Rating
import doobie.*
import doobie.implicits.*
import doobie.util.*

trait Ratings[F[_]]:
  def create(chartId: ChartId, lamp: Lamp, rating: Float): F[Unit]
  def forChartAndLamp(chartId: ChartId, lamp: Lamp): F[Float]

object Ratings:
  def apply[F[_]](using ratings: Ratings[F]) = ratings

final case class LiveRatings[F[_]: Concurrent](private val xa: Transactor[F])
    extends Ratings[F]:
  def create(chartId: ChartId, lamp: Lamp, rating: Float) =
    sql"""
      insert into rating
        ( chart_id
        , lamp_id
        , value )
      values
        ( ${chartId}
        , ${lamp}
        , ${rating} )
    """.update.run
      .transact(xa)
      .as(())

  def forChartAndLamp(chartId: ChartId, lamp: Lamp) =
    sql"""
      select value from rating
      where chart_id = $chartId and lamp_id = $lamp
    """
      .query[Float]
      .unique
      .transact(xa)
