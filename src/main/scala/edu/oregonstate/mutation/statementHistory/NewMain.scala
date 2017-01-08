package edu.oregonstate.mutation.statementHistory

import java.io.{BufferedInputStream, File, FileInputStream}

import com.brindescu.gumtree.facade.JavaASTDiff
import org.eclipse.jgit.api.Git
import play.api.libs.json._

object NewMain extends App {

	private case class Config(inputJSON: File = null,
														repoLocation: File = null) {	}

	private def parseCmdOptions(args: Array[String]): Option[Config] = {
		val parser = new scopt.OptionParser[Config]("java -jar <jarname>") {
			opt[String]('j', "json") action { (x, c) =>
				c.copy(inputJSON = new File(x))
			} validate( x => {
				val f = new File(x)
				if (f.exists && f.isFile)
					success
				else
					failure("The json file is not a file or doesn't exist")
			} ) text ("The json file with the lines to track") required()
			opt[String]('r', "repo") action { (x, c) =>
				c.copy(repoLocation = new File(x))
			} validate ( x => {
				val f = new File(x)
				if (f.exists)
					if (f.isDirectory)
						success
					else
						failure("The repository must be a directory")
				else
					failure("The given repo location does not exit")
			}) text("Repository location") required()
 		}

		parser.parse(args, Config())
	}

	case class FileToTrack(name: String, lines: List[Int])
	case class MergeCommit(sha: String, files: List[FileToTrack])

	override def main(args: Array[String]) = {
		val config = parseCmdOptions(args) match {
			case Some(c) => c
			case None => sys.exit(1)
		}

		val repo = Git.open(config.repoLocation)

		val detector = new NodeChangeDetector(repo, StatementFinder, JavaASTDiff)

		implicit val filesReader = Json.reads[FileToTrack]
		implicit val commitReader = Json.reads[MergeCommit]
		val parsed = Json.parse(new BufferedInputStream(new FileInputStream(config.inputJSON)))
		val commits = (parsed.validate[List[MergeCommit]] match {
			case s: JsSuccess[List[MergeCommit]] => s.get
			case _ =>
				println("Error parsing " + config.inputJSON)
				List[MergeCommit]()
		}).filterNot { _.files.isEmpty }
			.map (m => {
				MergeCommit(m.sha, m.files.filterNot { _.name == "dev/null" }
					.filterNot { _.lines.isEmpty }
				  .filter { _.name.endsWith("java")})
			})

		commits.toParArray.foreach(c => {
			c.files.foreach(f => {
				f.lines.foreach( l => {
					println(c.sha  + "," + f.name + "," + l + "," + detector.findCommits(f.name, l, c.sha, Order.FORWARD))
				})
			})
		})
	}

}
