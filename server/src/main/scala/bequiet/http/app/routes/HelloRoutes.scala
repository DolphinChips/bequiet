package bequiet.http.app.routes

import cats.Monad
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.scalatags.*

final class HelloRoutes[F[_]: Monad] extends Http4sDsl[F]:
  val routes = HttpRoutes.of[F]:
    case GET -> Root / "hello" / who =>
      Ok(bequiet.http.app.templates.hello(who))
