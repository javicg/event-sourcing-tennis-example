name := "TennisEventSource"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  // Main
  "com.typesafe.akka" %% "akka-persistence" % "2.5.3",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

  // Test
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % "test",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.1.1" % "test",
  "org.scalactic" %% "scalactic" % "3.0.1" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)