package bequiet.config

import cats.Show
import cats.derived.*
import com.zaxxer.hikari.HikariConfig

case class DatabaseConfig(uri: String, user: String, pass: String) derives Show

given Conversion[DatabaseConfig, HikariConfig] = conf =>
  val config = HikariConfig()
  config.setDriverClassName("org.postgresql.Driver")
  config.setSchema("bequiet")
  config.setJdbcUrl(conf.uri)
  config.setUsername(conf.user)
  config.setPassword(conf.pass)
  config
