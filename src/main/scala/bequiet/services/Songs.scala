package bequiet.services

// import cats.*
// import cats.implicits.*
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import bequiet.domain.song.*

trait Songs[F[_]]:
  // def all: F[List[Song]]
  def all: fs2.Stream[F, Song]
  def fromSongId(
      id: SongId
  ): F[Song] // Should it even be possible to get SongId without having Song?
  def find(id: Int): F[Option[Song]]
  // def create(artist: String, title: String): F[SongId] // shouldn't be exposed tbh
  // def filter: F[Filter[Song]]
  // def filtered(filter: Filter[Song]): fs2.Stream[F, Song]
  // def update(...): F[Option] // Also shouldn't be exposed

class LiveSongs[F[_]: Concurrent](private val xa: Transactor[F])
    extends Songs[F]:
  override def all =
    sql"""
      select
        song_id, title, artist
      from song
    """
      .query[Song]
      .stream
      .transact(xa)

  override def fromSongId(id: SongId) = ???

  override def find(id: Int) = ???
