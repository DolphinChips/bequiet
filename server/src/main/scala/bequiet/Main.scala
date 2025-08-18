package bequiet

import bequiet.config.{*, given}
import bequiet.domain.player.Player
import bequiet.http.api.routes.apiRoutes
import bequiet.http.app.routes.appRoutes
import bequiet.services.*
import cats.*
import cats.data.*
import cats.effect.*
import cats.syntax.all.*
import de.lhns.doobie.flyway.Flyway
import doobie.*
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{Logger => Http4sLogger}
import org.http4s.server.{Server, Router, AuthMiddleware}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp:
  given [F[_]: Sync] => Logger[F] = Slf4jLogger.getLogger[F]

  def makeDb[F[_]: Async](conf: Config): Resource[F, Transactor[F]] = for
    xa <- HikariTransactor.fromHikariConfig[F](conf.database)
    _ <- Resource
      .eval(sql"drop schema bequiet cascade".update.run.transact(xa))
      .whenA(conf.dev)
    _ <- Flyway(xa)(_.configure(_.validateMigrationNaming(true)).migrate())
  yield xa

  def authUser[F[_]: Applicative] =
    Kleisli(_ => OptionT.pure(Player(java.util.UUID(0, 0), "TESTTEST")))

  def middleware[F[_]: Monad] = AuthMiddleware(authUser)

  def makeHttpApp[F[_]: Async: Logger](xa: Transactor[F]) =
    val liveSongs = LiveSongs[F](xa)
    val livePlayers = LivePlayers[F](xa)
    val liveScores = LiveScores[F](xa)
    val liveCharts = LiveCharts[F](xa)
    val api = apiRoutes[F](liveSongs)
    val app = appRoutes[F](livePlayers, liveScores, liveSongs, liveCharts)
    Http4sLogger
      .httpRoutes[F](logHeaders = false, logBody = true)(
        Router(
          "/api" -> api,
          "/" -> app
        )
      )
      .orNotFound

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
    _ <- Logger[IO].info(show"Loaded config: $conf")
    _ <- makeServer[IO](conf).useForever
  yield ExitCode.Success
