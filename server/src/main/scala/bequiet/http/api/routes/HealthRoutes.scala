package bequiet
package http.api.routes

import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import cats.*
import cats.syntax.all.*
import cats.effect.Async

final class HealthRoutes[F[_]: Async]:
  val routes =
    val healthRoute = endpoint.get
      .in("health")
      .out(stringBody)
      .serverLogic(_ => Right[Unit, String]("").pure[F])
    Http4sServerInterpreter[F]().toRoutes(healthRoute)
