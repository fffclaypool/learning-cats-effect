name := "learning-cats-effect"

version := "0.1"
scalaVersion := "2.13.5"

val V = new {
  val circe = "0.13.0"
}

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect"          % "2.5.0",
  "io.circe"      %% "circe-core"           % V.circe,
  "io.circe"      %% "circe-generic"        % V.circe,
  "io.circe"      %% "circe-generic-extras" % V.circe,
  "io.circe"      %% "circe-parser"         % V.circe,
  "org.scalameta" %% "munit"                % "0.7.25"
)
