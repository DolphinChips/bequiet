package bequiet.http.app.routes

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.*
import bequiet.util.http4s.*
import bequiet.services.{Players, Scores}
import bequiet.domain.player.Player
import bequiet.http.app.templates
import java.util.UUID
import scala.util.Try
import org.http4s.scalatags.*

final case class PlayerRoutes[F[_]: Concurrent](
    private val players: Players[F],
    private val scores: Scores[F]
) extends Http4sDsl[F]:
  private def findPlayer(idStr: String): OptionT[F, Player] =
    OptionT(Try(UUID.fromString(idStr)).toOption.flatTraverse(players.find))

  private val playerRoute = HttpRoutes.of[F]:
    case GET -> Root / "player" / idStr =>
      findPlayer(idStr).map(templates.player).toResponse()

  private val playerScoresRoute = HttpRoutes.of[F]:
    case GET -> Root / "player" / idStr / "scores" =>
      findPlayer(idStr)
        .semiflatMap(
          scores
            .forPlayerId(_)
            .compile
            .toList
            .map(templates.scores)
        )
        .toResponse()

  val routes = playerRoute <+> playerScoresRoute
