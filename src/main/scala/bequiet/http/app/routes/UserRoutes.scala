package bequiet.http.app.routes

import cats.*
import cats.data.*
import cats.implicits.*
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.*
import bequiet.util.http4s.{*, given}
import bequiet.services.Players
import bequiet.services.Scores
import bequiet.domain.player.{Player, given}
import bequiet.http.app.templates.*
import java.util.UUID
import scala.util.Try

final case class PlayerRoutes[F[_]: Concurrent](
    private val players: Players[F],
    private val scores: Scores[F]
) extends Http4sDsl[F]:
  private def findPlayer(idStr: String): OptionT[F, Player] =
    OptionT
      .fromOption[F](
        Try(UUID.fromString(idStr)).toOption
      )
      .flatMapF(players.find)

  private val playerRoute = HttpRoutes.of[F]:
    case GET -> Root / "player" / idStr =>
      findPlayer(idStr).map(p => html.player(p)).toResponse()

  private val playerScoresRoute = HttpRoutes.of[F]:
    case GET -> Root / "player" / idStr / "scores" =>
      findPlayer(idStr)
        .semiflatMap(
          scores
            .forPlayerId(_)
            .compile
            .toList
            .map(s => html.scores(s))
        )
        .toResponse()

  val routes = playerRoute <+> playerScoresRoute
