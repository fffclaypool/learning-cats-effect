name := "learning-cats-effect"

version := "0.1"

scalaVersion := "2.13.5"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.1.0",
  "org.typelevel" %% "munit-cats-effect-3" % "0.13.1" % Test,
  "org.typelevel" %% "cats-effect-testing-specs2" % "1.0.0" % Test
)
