name := "untitled4"

version := "0.1"

scalaVersion := "2.12.8"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.7",
  
"com.typesafe.akka" %% "akka-stream" % "2.5.19" 
)
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


val circeVersion = "0.10.0"
libraryDependencies += "io.circe" %% "circe-optics" % circeVersion