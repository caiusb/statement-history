import sbt._

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.2.201509141540-r",
  "com.github.gumtreediff" % "core" % "2.0.0",
  "com.github.gumtreediff" % "gen.jdt" % "2.0.0"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

mainClass in Compile := Some("edu.oregonstate.mutation.statementHistory.Main")

lazy val root = (project in file(".")).
  settings(
    name := "statement-history",
    version := "0.1"
  )