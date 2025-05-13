package bequiet.http.app.routes

import cats.*
import org.http4s.*
import org.http4s.dsl.*
import bequiet.util.http4s.given

final class HelloRoutes[F[_]: Monad] extends Http4sDsl[F]:
  val routes = HttpRoutes.of[F]:
    case GET -> Root / "hello" / who =>
      Ok(bequiet.http.app.templates.html.Hello(who))
