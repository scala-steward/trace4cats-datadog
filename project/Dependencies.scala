import sbt._

object Dependencies {
  object Versions {
    val scala212 = "2.12.14"
    val scala213 = "2.13.6"

    val trace4cats = "0.12.0-RC1+146-d193db1e"

    val circe = "0.14.1"
    val http4s = "0.23.0-RC1"
  }

  lazy val trace4catsExporterCommon = "io.janstenpickle" %% "trace4cats-exporter-common" % Versions.trace4cats
  lazy val trace4catsExporterHttp = "io.janstenpickle"   %% "trace4cats-exporter-http"   % Versions.trace4cats
  lazy val trace4catsKernel = "io.janstenpickle"         %% "trace4cats-kernel"          % Versions.trace4cats
  lazy val trace4catsModel = "io.janstenpickle"          %% "trace4cats-model"           % Versions.trace4cats
  lazy val trace4catsTestkit = "io.janstenpickle"        %% "trace4cats-testkit"         % Versions.trace4cats

  lazy val circeGeneric = "io.circe"        %% "circe-generic-extras" % Versions.circe
  lazy val circeParser = "io.circe"         %% "circe-parser"         % Versions.circe
  lazy val http4sCirce = "org.http4s"       %% "http4s-circe"         % Versions.http4s
  lazy val http4sBlazeClient = "org.http4s" %% "http4s-blaze-client"  % Versions.http4s
}
