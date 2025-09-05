package bequiet.config

import cats.Show
import cats.derived.*

case class DatabaseConfig(uri: String, user: String, pass: String) derives Show
