name := """WebSocketsServer"""
organization := "com.home"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.1" % Test
libraryDependencies += "org.reactivemongo" % "play2-reactivemongo_2.12" % "0.12.6-play26"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.home.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.home.binders._"
