import sbt._

libraryDependencies ++= Seq(
	"org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.2" from "https://repo.eclipse.org/content/groups/releases//org/eclipse/jgit/org.eclipse.jgit/4.0.2.201509141540-r/org.eclipse.jgit-4.0.2.201509141540-r.jar",
	"com.github.gumtreediff" % "core" % "2.0.0",
	"com.github.gumtreediff" % "gen.jdt" % "2.0.0"
)

lazy val root = (project in file(".")).
	settings(
		name := "statement-history"
	)

