import sbt._

EclipseKeys.withSource := true

addCommandAlias("idea", "update-classifiers; update-sbt-classifiers; gen-idea sbt-classifiers")

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.2.201509141540-r",
  "org.eclipse.jdt" % "org.eclipse.jdt.core" % "3.10.0",
  "net.sf.trove4j" % "trove4j" % "3.0.3",
  "com.typesafe.play" %% "play-json" % "2.4.3",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "edu.illinois.wala" %% "walafacade" % "0.1.2"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

resolvers += Resolver.sonatypeRepo("public")

val mc = Some("edu.oregonstate.mutation.statementHistory.Main")

mainClass in (Compile, run) := mc

mainClass in assembly := mc

lazy val root = (project in file(".")).
  settings(
    name := "statement-history",
    version := "0.6.2"
  )

lazy val versionReport = TaskKey[String]("version-report")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}