import bequiet.config.Config
import bequiet.doobie.given
import bequiet.doobie.services.{LiveSongs, LiveCharts, LiveRatings}
import bequiet.services.{Songs, Charts, Ratings}
import bequiet.cli.subcommands.*
import cats.effect.*
import cats.syntax.all.*
import com.monovore.decline.*
import com.monovore.decline.effect.*
import fs2.io.file.{Files, Path}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import de.lhns.doobie.flyway.Flyway
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.implicits.*

object Main
    extends CommandIOApp(
      name = "bequietctl",
      header = "Be quiet command line"
    ):
  given [F[_]: Sync] => Logger[F] = Slf4jLogger.getLogger[F]

  def makeDb[F[_]: Async](conf: Config): Resource[F, Transactor[F]] = for
    xa <- HikariTransactor.fromHikariConfig[F](conf.database)
    _ <- Resource
      .eval(sql"drop schema bequiet cascade".update.run.transact(xa))
      .whenA(conf.dev)
    _ <- Flyway(xa)(_.configure(_.validateMigrationNaming(true)).migrate())
  yield xa

  override def main: Opts[IO[ExitCode]] =
    (ImportMdb.opts orElse UpdateCPI.opts).map:
      case ImportMdb(path) =>
        val f = Files[IO].readAll(Path(path))
        for
          conf <- Config.loadF[IO]
          _ <- Logger[IO].info(show"Loaded config: $conf")
          e <- makeDb[IO](conf).use { xa =>
            given Songs[IO] = LiveSongs[IO](xa)
            given Charts[IO] = LiveCharts[IO](xa)
            ImportMdb.run(f)
          }
        yield e
      case UpdateCPI() =>
        for
          conf <- Config.loadF[IO]
          _ <- Logger[IO].info(show"Loaded config: $conf")
          e <- makeDb[IO](conf).use { xa =>
            given Songs[IO] = LiveSongs[IO](xa)
            given Charts[IO] = LiveCharts[IO](xa)
            given Ratings[IO] = LiveRatings[IO](xa)
            UpdateCPI.run()
          }
        yield e
