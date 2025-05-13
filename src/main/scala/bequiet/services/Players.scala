package bequiet.services

import cats.effect.*
import bequiet.domain.player.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import doobie.postgres.implicits.*
import java.util.UUID

trait Players[F[_]]:
  def fromPlayerId(playerId: PlayerId): F[Player]
  def find(playerId: PlayerId): F[Option[Player]]

final case class LivePlayers[F[_]: Concurrent](private val xa: Transactor[F])
    extends Players[F]:
  def findUserQuery(playerId: PlayerId) =
    sql"""
      select
        player_id, djname
      from player
      where player_id = $playerId
    """
      .query[Player]

  override def fromPlayerId(playerId: PlayerId) =
    findUserQuery(playerId).unique
      .transact(xa)

  override def find(playerId: PlayerId) =
    findUserQuery(playerId).option
      .transact(xa)
