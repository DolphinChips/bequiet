package bequiet.util.http4s

import cats.{Monad, Applicative}
import cats.data.OptionT
import cats.implicits.*
import io.circe.Encoder
import org.http4s.Response
import org.http4s.Status
import org.http4s.Status.*
import org.http4s.circe.streamJsonArrayEncoderOf
import org.http4s.EntityEncoder
import org.http4s.Headers
import org.http4s.headers.{`Content-Type`, `Content-Length`}
import org.http4s.MediaType
import org.http4s.Charset.`UTF-8`
import play.twirl.api.{Content, Html}

extension (s: Status)
  def apply[F[_]: Applicative] =
    Response[F](s, headers = Headers(List(`Content-Length`.zero))).pure[F]
  def apply[F[_]: Applicative, A](body: A)(using ee: EntityEncoder[F, A]) =
    val entity = ee.toEntity(body)
    val headers = Headers(List(`Content-Length`(entity.length.getOrElse(0))))
    Response[F](status = s, body = entity.body, headers = headers).pure[F]

extension [F[_]: Monad, A](
    o: OptionT[F, A]
)(using EntityEncoder[F, A])(using Applicative[F])
  def toResponse(default: => F[Response[F]] = NotFound[F]): F[Response[F]] =
    o.value.flatMap:
      case Some(e) => Ok(e)
      case None    => default

private def contentEncoder[F[_], C <: Content] =
  EntityEncoder[F, String].contramap[C](_.body)
given htmlEncoder: [F[_]] => EntityEncoder[F, Html] =
  contentEncoder[F, Html]
    .withContentType(`Content-Type`(MediaType.text.html, `UTF-8`))

given streamEncoder: [F[_], A: Encoder] => EntityEncoder[F, fs2.Stream[F, A]] =
  streamJsonArrayEncoderOf[F, A]
