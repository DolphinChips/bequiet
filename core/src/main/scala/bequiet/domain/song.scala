package bequiet.domain

import cats.Show
import cats.derived.*

object song:
  type SongId = Int
  case class Song(
      id: SongId,
      artist: String,
      title: String
  ) derives Show

  object Song:
    given Conversion[Song, SongId] = _.id
