package bequiet.domain.song

type SongId = Int
case class Song(
    id: SongId,
    artist: String,
    title: String
)

object Song:
  given Conversion[Song, SongId] = _.id
