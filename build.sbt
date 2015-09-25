import sbt._

EclipseKeys.withSource := true

addCommandAlias("idea", "update-classifiers; update-sbt-classifiers; gen-idea sbt-classifiers")

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.2.201509141540-r",
  "org.eclipse.jdt" % "org.eclipse.jdt.core" % "3.10.0",
  "net.sf.trove4j" % "trove4j" % "3.0.3"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

mainClass in Compile := Some("edu.oregonstate.mutation.statementHistory.Main")

lazy val root = (project in file(".")).
  settings(
    name := "statement-history",
    version := "0.1"
  )