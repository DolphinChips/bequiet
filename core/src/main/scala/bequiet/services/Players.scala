package bequiet.services

import cats.effect.*
import bequiet.domain.player.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import doobie.postgres.implicits.*
import java.util.UUID

trait Players[F[_]]:
  def create(djName: String): F[PlayerId]
  def find(playerId: UUID): F[Option[Player]]

final case class LivePlayers[F[_]: Concurrent](private val xa: Transactor[F])
    extends Players[F]:
  def findUserQuery(playerId: UUID) =
    sql"""
      select
        player_id, djname
      from player
      where player_id = $playerId
    """
      .query[Player]

  override def create(djName: String) =
    sql"""
      insert into player(djname) values($djName)
    """.update
      .withUniqueGeneratedKeys[UUID]("player_id")
      .transact(xa)

  override def find(playerId: UUID) =
    findUserQuery(playerId).option
      .transact(xa)
