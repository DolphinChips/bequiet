package bequiet.util.http4s

import cats.{Monad, Applicative}
import cats.data.OptionT
import cats.syntax.all.*
import io.circe.Encoder
import org.http4s.Response
import org.http4s.Status
import org.http4s.Status.*
import org.http4s.circe.streamJsonArrayEncoderOf
import org.http4s.EntityEncoder
import org.http4s.Headers
import org.http4s.headers.`Content-Length`

extension (s: Status)
  private def apply[F[_]: Applicative] =
    Response[F](s, headers = Headers(List(`Content-Length`.zero))).pure[F]
  private def apply[F[_]: Applicative, A](body: A)(using
      ee: EntityEncoder[F, A]
  ) =
    val entity = ee.toEntity(body)
    val headers = Headers(entity.length.map(`Content-Length`.apply).toList)
    Response[F](status = s, body = entity.body, headers = ee.headers ++ headers)
      .pure[F]

extension [F[_]: Monad, A](
    o: OptionT[F, A]
)(using EntityEncoder[F, A])(using Applicative[F])
  def toResponse(default: => F[Response[F]] = NotFound[F]): F[Response[F]] =
    o.value.flatMap:
      case Some(e) => Ok(e)
      case None    => default

given streamEncoder: [F[_], A: Encoder] => EntityEncoder[F, fs2.Stream[F, A]] =
  streamJsonArrayEncoderOf[F, A]
