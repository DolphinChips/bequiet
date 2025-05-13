package bequiet.config

import cats.Show
import cats.derived.*
import cats.effect.*
import pureconfig.*
import pureconfig.module.catseffect.syntax.*
import pureconfig.module.ip4s.*
import com.comcast.ip4s.{Host, Port}

case class Config(
    database: DatabaseConfig,
    dev: Boolean = false,
    host: Host,
    port: Port
) derives ConfigReader,
      Show

object Config:
  def loadF[F[_]: Sync]: F[Config] = ConfigSource.default.loadF[F, Config]()
