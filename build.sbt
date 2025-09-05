import org.typelevel.sbt.tpolecat.*
import org.typelevel.scalacoptions.ScalacOptions
import org.typelevel.scalacoptions.ScalacOption

ThisBuild / scalaVersion := "3.7.2"
ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.7"

ThisBuild / Compile / run / fork := true

lazy val CEVersion = "3.6.3"
lazy val CirceVersion = "0.14.14"
lazy val DeclineVersion = "2.5.0"
lazy val DoobieFlywayVersion = "0.5.3"
lazy val DoobieVersion = "1.0.0-RC10"
lazy val FlywayVersion = "11.11.1"
lazy val Fs2Version = "3.12.0"
lazy val Fs2DataVersion = "1.12.0"
lazy val Http4sScalatagsVersion = "0.25.2"
lazy val Http4sVersion = "0.23.30"
lazy val JunixsocketVersion = "2.10.1"
lazy val KittensVersion = "3.5.0"
lazy val Log4CatsVersion = "2.7.1"
lazy val LogbackVersion = "1.5.18"
lazy val PureconfigVersion = "0.17.9"
lazy val ScalatagsVersion = "0.13.1"
lazy val ScodecVersion = "2.3.3"
lazy val TapirVersion = "1.11.42"

lazy val core = (project in file("core"))
  .settings(
    name := "bequiet-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "kittens" % KittensVersion,
      "com.github.pureconfig" %% "pureconfig-core" % PureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % PureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-ip4s" % PureconfigVersion,
      "co.fs2" %% "fs2-core" % Fs2Version
    )
  )

lazy val doobie = (project in file("doobie"))
  .settings(
    name := "bequiet-doobie",
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
      "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
      "org.typelevel" %% "log4cats-core" % Log4CatsVersion
    )
  )
  .dependsOn(core)

lazy val server = (project in file("server"))
  .settings(
    name := "bequiet-server",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % CEVersion,
      "org.typelevel" %% "cats-effect-kernel" % CEVersion,
      "org.typelevel" %% "cats-effect-std" % CEVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "de.lhns" %% "doobie-flyway" % DoobieFlywayVersion,
      "org.flywaydb" % "flyway-database-postgresql" % FlywayVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % TapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % TapirVersion,
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-scalatags" % Http4sScalatagsVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "com.lihaoyi" %% "scalatags" % ScalatagsVersion,
      "com.kohlschutter.junixsocket" % "junixsocket-core" % JunixsocketVersion
    )
  )
  .dependsOn(core)
  .dependsOn(doobie)

lazy val cli = (project in file("cli"))
  .settings(
    name := "bequietctl",
    libraryDependencies ++= Seq(
      "com.monovore" %% "decline-effect" % DeclineVersion,
      "co.fs2" %% "fs2-io" % Fs2Version,
      "co.fs2" %% "fs2-scodec" % Fs2Version,
      "org.scodec" %% "scodec-core" % ScodecVersion,
      "org.gnieh" %% "fs2-data-csv" % Fs2DataVersion,
      "de.lhns" %% "doobie-flyway" % DoobieFlywayVersion,
      "org.flywaydb" % "flyway-database-postgresql" % FlywayVersion,
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.kohlschutter.junixsocket" % "junixsocket-core" % JunixsocketVersion,
      "org.typelevel" %% "log4cats-slf4j" % Log4CatsVersion
    )
  )
  .dependsOn(core)
  .dependsOn(doobie)
