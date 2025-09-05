package bequiet.services

import bequiet.domain.score.*
import bequiet.domain.player.PlayerId
import bequiet.domain.chart.ChartId

trait Scores[F[_]]:
  def create(playerId: PlayerId, chartId: ChartId, lamp: Lamp): F[ScoreId]
  def forPlayerId(playerId: PlayerId): fs2.Stream[F, Score]
