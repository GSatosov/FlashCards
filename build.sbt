name := "FlashcardsApp"

version := "0.1"

scalaVersion := "2.12.4"

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeVersion)
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.4",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11" % Test,
  "org.postgresql" % "postgresql" % "42.1.4",
  "com.github.tminglei" %% "slick-pg" % "0.15.4",
  "com.softwaremill.akka-http-session" %% "core" % "0.5.1",
  "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",
  "org.slf4j" % "slf4j-simple" % "1.7.21")

enablePlugins(JavaAppPackaging)