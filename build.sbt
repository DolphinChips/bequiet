import org.typelevel.sbt.tpolecat.*
import org.typelevel.scalacoptions.ScalacOptions

ThisBuild / scalaVersion := "3.7.0"

Compile / run / fork := true
Compile / tpolecatExcludeOptions += ScalacOptions.warnUnusedImports

lazy val CEVersion = "3.6.1"
lazy val CETestingSpecs2Version = "1.6.0"
lazy val KittensVersion = "3.5.0"
lazy val MunitCEVersion = "2.1.0"
lazy val PureconfigVersion = "0.17.8"
lazy val DoobieVersion = "1.0.0-RC8"
lazy val Log4CatsVersion = "2.7.0"
lazy val FlywayVersion = "11.7.1"
lazy val DoobieFlywayVersion = "0.5.1"
lazy val TapirVersion = "1.11.25"
lazy val Http4sVersion = "0.23.30"
lazy val CirceVersion = "0.14.13"
lazy val LogbackVersion = "1.5.18"

lazy val root = (project in file("."))
  .enablePlugins(SbtTwirl)
  .settings(
    name := "be-quiet",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CEVersion,
      "org.typelevel" %% "cats-effect-kernel" % CEVersion,
      "org.typelevel" %% "cats-effect-std" % CEVersion,
      "org.typelevel" %% "cats-effect-testing-specs2" % CETestingSpecs2Version % Test,
      "org.typelevel" %% "munit-cats-effect" % MunitCEVersion % Test,
      "org.typelevel" %% "kittens" % KittensVersion,
      "com.github.pureconfig" %% "pureconfig-core" % PureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-ip4s" % PureconfigVersion,
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "org.typelevel" %% "log4cats-slf4j" % Log4CatsVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.flywaydb" % "flyway-database-postgresql" % FlywayVersion,
      "de.lhns" %% "doobie-flyway" % DoobieFlywayVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % TapirVersion,
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion)
  )
