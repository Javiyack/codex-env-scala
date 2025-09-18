ThisBuild / scalaVersion := "2.13.12"

lazy val Migrations = config("migrations") extend Compile

lazy val root = (project in file("."))
  .configs(Migrations)
  .settings(
    name := "event-node-app",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.4.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
      "org.postgresql" % "postgresql" % "42.7.3",
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",
      "io.github.nafg" %% "slick-migration-api" % "0.9.0"
    )
  )
  .settings(inConfig(Migrations)(Defaults.configSettings): _*)
