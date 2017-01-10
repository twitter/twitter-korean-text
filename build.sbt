name := "Korean Text Processing Utilities SBT"

version := "4.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.twitter" % "twitter-text" % "1.11.1",
  "org.slf4j" % "slf4j-nop" % "1.5.8" % "provided",
  "com.github.nscala-time"  %% "nscala-time"   % "2.14.0" % "provided",
  "org.scalatest"     %% "scalatest"   % "3.0.0" % "test",
  "junit"             %  "junit"       % "4.12"  % "test"
)