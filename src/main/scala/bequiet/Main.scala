package bequiet

import cats.*
import cats.implicits.*
import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.hikari.HikariTransactor
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import de.lhns.doobie.flyway.Flyway
import bequiet.config.{*, given}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Server, Router}
import fs2.io.net.Network
import bequiet.http.api.routes.apiRoutes
import bequiet.http.app.routes.appRoutes
import bequiet.services.*

object Main extends IOApp:
  given [F[_]: Sync] => Logger[F] = Slf4jLogger.getLogger[F]

  def makeDb[F[_]: Async](conf: Config): Resource[F, Transactor[F]] = for
    xa <- HikariTransactor.fromHikariConfig[F](conf.database)
    _ <- Resource
      .eval(sql"drop schema bequiet cascade".update.run.transact(xa))
      .whenA(conf.dev)
    _ <- Flyway(xa)(_.configure(_.validateMigrationNaming(true)).migrate())
  yield xa

  def makeHttpApp[F[_]: Async](xa: Transactor[F]) =
    val liveSongs = LiveSongs[F](xa)
    val livePlayers = LivePlayers[F](xa)
    val liveScores = LiveScores[F](xa)
    val api = apiRoutes[F](liveSongs)
    val app = appRoutes[F](livePlayers, liveScores)
    Router(
      "/api" -> api,
      "/" -> app
    ).orNotFound

  def makeServer[F[_]: Async: Network](conf: Config): Resource[F, Server] = for
    db <- makeDb[F](conf)
    server <- EmberServerBuilder
      .default[F]
      .withHost(conf.host)
      .withPort(conf.port)
      .withHttpApp(makeHttpApp[F](db))
      .build
  yield server

  def run(args: List[String]): IO[ExitCode] = for
    conf <- Config.loadF[IO]
    _ <- Logger[IO].info(s"Loaded config: ${conf.show}")
    _ <- makeServer[IO](conf).useForever
  yield ExitCode.Success
