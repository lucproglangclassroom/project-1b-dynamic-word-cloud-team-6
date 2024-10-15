name := "topwords"

version := "0.3"

scalaVersion := "3.4.2"

scalacOptions += "@.scalacOptions.txt"

libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"  % "3.2.19"  % Test,
  "org.scalacheck" %% "scalacheck" % "1.18.0" % Test,
  "junit" % "junit" % "4.13.2" % Test,
  "org.apache.commons" % "commons-collections4" % "4.4",
  "com.lihaoyi" %% "mainargs" % "0.6.3",
  "org.log4s" %% "log4s" % "1.10.0",
  "org.slf4j" % "slf4j-simple" % "1.7.30",
  "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
)

coverageExcludedPackages := ".*Main*"


enablePlugins(JavaAppPackaging)
