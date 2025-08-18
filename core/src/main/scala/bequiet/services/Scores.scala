package bequiet.services

import cats.effect.*
import bequiet.domain.score.*
import bequiet.domain.player.PlayerId
import doobie.*
import doobie.implicits.*
import doobie.util.*
import doobie.postgres.implicits.*

trait Scores[F[_]]:
  def forPlayerId(playerId: PlayerId): fs2.Stream[F, Score]

final case class LiveScores[F[_]: Concurrent](private val xa: Transactor[F])
    extends Scores[F]:
  def forPlayerId(playerId: PlayerId) =
    sql"""
      select
        score_id, lamp_id
      from score
      where player_id = $playerId
      """
      .query[Score]
      .stream
      .transact(xa)
