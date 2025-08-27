package bequiet.services

import cats.*
import cats.syntax.all.*
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.util.*
import bequiet.domain.song.*
import org.typelevel.log4cats.Logger

trait Songs[F[_]]:
  def create(id: SongId, artist: String, title: String): F[Unit]
  def all: fs2.Stream[F, Song]
  def fromSongId(
      id: SongId
  ): F[Song]
  def find(id: Int): F[Option[Song]]
  def find(title: String): F[Option[Song]]

object Songs:
  def apply[F[_]](using songs: Songs[F]) = songs

class LiveSongs[F[_]: Concurrent: Logger](private val xa: Transactor[F])
    extends Songs[F]:
  private val selectSongFragment =
    fr"""
      select
        song_id, artist, title
      from song
    """

  private def findSongByIdQuery(id: Int) =
    val whereFragment = fr"""
      where song_id = $id
    """
    val statement = selectSongFragment |+| whereFragment
    statement.query[Song]

  override def create(id: SongId, artist: String, title: String) =
    sql"""
      insert into song
        ( song_id
        , title
        , artist )
      values
        ( ${id}
        , ${title}
        , ${artist} )
    """.update.run
      .transact(xa)
      .map(_ => ())

  override def all =
    selectSongFragment
      .query[Song]
      .stream
      .transact(xa)

  override def fromSongId(id: SongId) =
    findSongByIdQuery(id).unique
      .transact(xa)

  override def find(id: Int) =
    findSongByIdQuery(id).option
      .transact(xa)

  override def find(title: String) =
    val whereFragment = fr"""
      where title = $title
    """
    val statement = selectSongFragment |+| whereFragment
    statement.query[Song].option.transact(xa).flatMap {
      case x: Some[Song] => x.pure[F]
      case None          => Logger[F].info(s"\\$title\\").as(None)
    }
