enablePlugins(JavaAppPackaging)

organization := "org.aj"

name := """ner-service"""

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV       = "2.4.11"
  val scalaTestV  = "3.0.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaV
  )
}

Revolver.settings
