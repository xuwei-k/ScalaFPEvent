name := "ScalaFPEvent"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq("io.reactivex" %% "rxscala" % "0.25.0",
  "org.scalaz" %% "scalaz-core" % "7.1.3",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.3",
  "org.slf4j" % "slf4j-api" % "1.7.12",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)
    