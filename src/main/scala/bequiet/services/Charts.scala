package bequiet.services

import bequiet.domain.chart.*
import bequiet.domain.song.SongId

trait Charts[F[_]]:
  def all: fs2.Stream[F, Chart]
  def forSongId(songId: SongId): fs2.Stream[F, Chart] // should be in filter
  def fromChartId(id: ChartId): F[Chart]
  def find(id: Int): F[Option[Chart]]
  // def filter: F[Filter[Song]]
