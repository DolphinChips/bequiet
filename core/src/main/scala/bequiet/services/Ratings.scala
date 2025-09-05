package bequiet.services

import bequiet.domain.score.Lamp
import bequiet.domain.chart.ChartId

trait Ratings[F[_]]:
  def create(chartId: ChartId, lamp: Lamp, rating: Float): F[Unit]
  def find(chartId: ChartId, lamp: Lamp): F[Option[Float]]

object Ratings:
  def apply[F[_]](using ratings: Ratings[F]) = ratings
