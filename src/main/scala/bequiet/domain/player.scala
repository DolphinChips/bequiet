package bequiet.domain.player

import java.util.UUID

type PlayerId = UUID
object PlayerId:
  def apply(x: UUID): PlayerId = x

case class Player(
    id: PlayerId,
    djName: String
)

object Player:
  given Conversion[Player, PlayerId] = _.id
