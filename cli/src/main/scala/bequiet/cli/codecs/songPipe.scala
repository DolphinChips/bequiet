package bequiet.cli.codecs

import cats.Show
import cats.derived.*
import cats.syntax.all.*
import scodec.Codec
import scodec.bits.*
import scodec.codecs.*
import java.nio.charset.Charset
import fs2.*
import fs2.interop.scodec.*

case class Header(song_count: Long, song_id_bank: List[Int]) derives Show

object Header:
  private val magic = constant(hex"49494458")
  private val version = constant(hex"20000000")

  val codec: Codec[Header] = "header" | {
    ("magic" | magic) ::
      ("version" | version) ::
      ("song-count" | uint32L) ::
      ("song-id-bank" | listOfN(intL(32), int32L))
  }.dropUnits.as[Header]

case class Levels(
    b: Option[Int],
    n: Option[Int],
    h: Option[Int],
    a: Option[Int],
    l: Option[Int]
):
  def toList: List[(String, Int)] =
    List(
      ("B", b),
      ("N", n),
      ("H", h),
      ("A", a),
      ("L", l)
    ).collect({ case (diff, Some(rating)) =>
      diff -> rating
    })

object Levels:
  given Show[Levels] = Show.show { levels =>
    val fields = levels.toList
      .map { case (diff, rating) =>
        show"$diff = $rating"
      }
      .mkString(", ")
    "Levels(" ++ fields ++ ")"
  }

  def ofMap(map: Map[String, Int]) = Levels(
    b = map.get("B"),
    n = map.get("N"),
    h = map.get("H"),
    a = map.get("A"),
    l = map.get("L")
  )

  private val levelCodec =
    uint8.xmap[Option[Int]](
      {
        case 0 => None
        case i => Some(i)
      },
      _.getOrElse(0)
    )

  val codec: Codec[Levels] = "levels" | {
    ("b" | levelCodec) ::
      ("n" | levelCodec) ::
      ("h" | levelCodec) ::
      ("a" | levelCodec) ::
      ("l" | levelCodec)
  }.as[Levels]

case class Song(
    title: String,
    artist: String,
    game_version: Int,
    levelsSP: Levels,
    levelsDP: Levels
) derives Show

object Song:
  private val stringCodec =
    fixedSizeBytes(0x100, string(Charset.forName("UTF-16LE")))

  val codec: Codec[Song] = "song" | {
    ("title" | stringCodec) ::
      reserved(0x40 + 0x80) ::
      ("artist" | stringCodec) ::
      ("subtitle" | reserved(0x100)) ::
      reserved(4 * 7) ::
      ("game-version" | uint16L) ::
      reserved(2 * 7) ::
      ("levels-sp" | Levels.codec) ::
      ("levels-dp" | Levels.codec) ::
      reserved(0x402)
  }.as[Song]

def songPipe[F[_]: RaiseThrowable]: Pipe[F, Byte, Song] =
  StreamDecoder
    .once(Header.codec)
    .flatMap { _ =>
      StreamDecoder.many(Song.codec)
    }
    .toPipeByte[F](using summon)
