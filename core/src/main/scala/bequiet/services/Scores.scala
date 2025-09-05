package bequiet.services

import cats.effect.*
import bequiet.domain.score.*
import bequiet.domain.player.PlayerId
import bequiet.domain.chart.ChartId
import doobie.*
import doobie.implicits.*
import doobie.util.*
import doobie.postgres.implicits.*

trait Scores[F[_]]:
  def create(playerId: PlayerId, chartId: ChartId, lamp: Lamp): F[ScoreId]
  def forPlayerId(playerId: PlayerId): fs2.Stream[F, Score]

final case class LiveScores[F[_]: Concurrent](private val xa: Transactor[F])
    extends Scores[F]:
  override def create(playerId: PlayerId, chartId: ChartId, lamp: Lamp) =
    ConnectionIOScores.create(playerId, chartId, lamp).transact(xa)

  override def forPlayerId(playerId: PlayerId) =
    ConnectionIOScores.forPlayerId(playerId).transact(xa)

object ConnectionIOScores extends Scores[ConnectionIO]:
  override def create(playerId: PlayerId, chartId: ChartId, lamp: Lamp) =
    sql"""
      insert into score
        ( player_id
        , chart_id
        , lamp_id )
      values
        ( $playerId
        , $chartId
        , $lamp )
    """.update
      .withUniqueGeneratedKeys[ScoreId]("score_id")

  override def forPlayerId(playerId: PlayerId) =
    sql"""
      select
        score_id, lamp_id
      from score
      where player_id = $playerId
      """
      .query[Score]
      .stream
