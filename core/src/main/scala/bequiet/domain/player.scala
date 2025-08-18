package bequiet.domain

import java.util.UUID

object player:
  type PlayerId = UUID
  object PlayerId:
    def apply(x: UUID): PlayerId = x

  case class Player(
      id: PlayerId,
      djName: String
  )

  object Player:
    given Conversion[Player, PlayerId] = _.id
