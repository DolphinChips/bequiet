package bequiet.doobie.services

import bequiet.doobie.given
import bequiet.domain.chart.*
import bequiet.domain.player.*
import bequiet.domain.score.*
import bequiet.domain.song.*
import bequiet.services.*
import cats.syntax.all.*
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import org.typelevel.log4cats.Logger
import java.util.UUID

final case class LiveCharts[F[_]: Concurrent](private val xa: Transactor[F])
    extends Charts[F]:
  override def create(
      songId: SongId,
      playstyle: Playstyle,
      difficulty: Difficulty
  ) =
    ConnectionIOCharts.create(songId, playstyle, difficulty).transact(xa)

  override def all =
    ConnectionIOCharts.all.transact(xa)

  override def forSongId(songId: SongId) =
    ConnectionIOCharts.forSongId(songId).transact(xa)

  override def find(id: Int) =
    ConnectionIOCharts.find(id).transact(xa)

object ConnectionIOCharts extends Charts[ConnectionIO]:
  def findChartQuery(chartId: Int) =
    sql"""
      select
        chart_id, playstyle, difficulty
      from chart
      where chart_id = $chartId
    """
      .query[Chart]

  override def create(
      songId: SongId,
      playstyle: Playstyle,
      difficulty: Difficulty
  ) =
    sql"""
      insert into chart
        ( song_id
        , playstyle
        , difficulty )
      values
        ( ${songId}
        , ${playstyle}
        , ${difficulty} )
    """.update
      .withUniqueGeneratedKeys[Int]("chart_id")

  override def all =
    sql"""
      select
        chart_id, playstyle, difficulty
      from chart
    """
      .query[Chart]
      .stream

  override def forSongId(songId: SongId) =
    sql"""
      select
        chart_id, playstyle, difficulty
      from chart
      where song_id = $songId
    """
      .query[Chart]
      .to[List]

  override def find(id: Int) =
    findChartQuery(id).option

final case class LivePlayers[F[_]: Concurrent](private val xa: Transactor[F])
    extends Players[F]:
  override def create(djName: String) =
    ConnectionIOPlayers.create(djName).transact(xa)

  override def find(playerId: UUID) =
    ConnectionIOPlayers.find(playerId).transact(xa)

object ConnectionIOPlayers extends Players[ConnectionIO]:
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

  override def find(playerId: UUID) =
    findUserQuery(playerId).option

final case class LiveRatings[F[_]: Concurrent](private val xa: Transactor[F])
    extends Ratings[F]:
  def create(chartId: ChartId, lamp: Lamp, rating: Float) =
    ConnectionIORatings.create(chartId, lamp, rating).transact(xa)

  def find(chartId: ChartId, lamp: Lamp) =
    ConnectionIORatings.find(chartId, lamp).transact(xa)

object ConnectionIORatings extends Ratings[ConnectionIO]:
  def create(chartId: ChartId, lamp: Lamp, rating: Float) =
    sql"""
      insert into rating
        ( chart_id
        , lamp_id
        , value )
      values
        ( ${chartId}
        , ${lamp}
        , ${rating} )
    """.update.run
      .as(())

  def find(chartId: ChartId, lamp: Lamp) =
    sql"""
      select value from rating
      where chart_id = $chartId and lamp_id = $lamp
    """
      .query[Float]
      .option

final case class LiveScores[F[_]: Concurrent](private val xa: Transactor[F])
    extends Scores[F]:
  override def create(playerId: PlayerId, chartId: ChartId, lamp: Lamp) =
    ConnectionIOScores.create(playerId, chartId, lamp).transact(xa)

  override def forPlayerId(playerId: PlayerId) =
    ConnectionIOScores.forPlayerId(playerId).transact(xa)

object ConnectionIOScores extends Scores[ConnectionIO]:
  override def create(playerId: PlayerId, chartId: ChartId, lamp: Lamp) =
    sql"""
      insert into score
        ( player_id
        , chart_id
        , lamp_id )
      values
        ( $playerId
        , $chartId
        , $lamp )
    """.update
      .withUniqueGeneratedKeys[ScoreId]("score_id")

  override def forPlayerId(playerId: PlayerId) =
    sql"""
      select
        score_id, lamp_id
      from score
      where player_id = $playerId
      """
      .query[Score]
      .stream

class LiveSongs[F[_]: Concurrent: Logger](private val xa: Transactor[F])
    extends Songs[F]:
  override def create(id: SongId, artist: String, title: String) =
    ConnectionIOSongs.create(id, artist, title).transact(xa)

  override def all =
    ConnectionIOSongs.all.transact(xa)

  override def find(id: Int) =
    ConnectionIOSongs.find(id).transact(xa)

  override def find(title: String) =
    ConnectionIOSongs.find(title).transact(xa).flatMap {
      case x: Some[Song] => x.pure[F]
      case None          => Logger[F].info(s"\\$title\\").as(None)
    }

object ConnectionIOSongs extends Songs[ConnectionIO]:
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
      .as(())

  override def all =
    selectSongFragment
      .query[Song]
      .stream

  override def find(id: Int) =
    findSongByIdQuery(id).option

  override def find(title: String) =
    val whereFragment = fr"""
      where title = $title
    """
    val statement = selectSongFragment |+| whereFragment
    statement.query[Song].option
