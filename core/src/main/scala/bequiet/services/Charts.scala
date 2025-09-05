package bequiet.services

import bequiet.domain.chart.*
import bequiet.domain.song.SongId

trait Charts[F[_]]:
  def create(
      songId: SongId,
      playstyle: Playstyle,
      difficulty: Difficulty
  ): F[Int]
  def all: fs2.Stream[F, Chart]
  def forSongId(songId: SongId): F[List[Chart]]
  def find(id: Int): F[Option[Chart]]

object Charts:
  def apply[F[_]](using charts: Charts[F]) = charts
