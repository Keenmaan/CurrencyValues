name := """CurrencyValues"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  javaJpa
)

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "com.google.code.gson" % "gson" % "2.3.1"

