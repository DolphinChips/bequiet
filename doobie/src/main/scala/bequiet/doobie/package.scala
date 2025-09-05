package bequiet.doobie

import bequiet.config.DatabaseConfig
import bequiet.domain.chart.{Difficulty, Playstyle}
import bequiet.domain.score.Lamp
import doobie.Meta
import com.zaxxer.hikari.HikariConfig

given Conversion[DatabaseConfig, HikariConfig] = conf =>
  val config = HikariConfig()
  config.setDriverClassName("org.postgresql.Driver")
  config.setSchema("bequiet")
  config.setJdbcUrl(conf.uri)
  config.setUsername(conf.user)
  config.setPassword(conf.pass)
  config

given Meta[Difficulty] =
  Meta[String].timap(Difficulty.fromString)(Difficulty.toString)
given Meta[Lamp] = Meta[Int].timap(Lamp.fromInt)(Lamp.toInt)
given Meta[Playstyle] =
  Meta[String].timap(Playstyle.fromString)(Playstyle.toString)
