package bequiet.services

import bequiet.domain.player.*
import java.util.UUID

trait Players[F[_]]:
  def create(djName: String): F[PlayerId]
  def find(playerId: UUID): F[Option[Player]]
