package edu.oregonstate.mutation.statementHistory

import java.io.{File, FileOutputStream, PrintStream}
import java.util.logging.Level

import fr.labri.gumtree.matchers.Matcher

object Main {

  private[statementHistory] case class Config(method: Boolean = false,
                        repo: File = new File("."),
                        jsonFile: Option[File] = None,
                        commit: String = "HEAD",
                        file:Option[String] = None,
                        forward: Boolean = false,
                        reverse: Boolean = false) {
  }

  private[statementHistory] def parseCmdOptions(args: Array[String]): Option[Config] = {
    val parser = new scopt.OptionParser[Config]("java -jar <jar_name>") {
      opt[Boolean]('m', "method") action { (x,c) =>
          c.copy(method = x)
        } text ("track methods; default is false, it tracks statements")
      opt[String]('r', "repo") required() action { (x,c) =>
        c.copy(repo=new File(x))
      } text("The location of the repository")
      opt[String]('j', "json-file") action { (x,c) =>
        c.copy(jsonFile = Some(new File(x)))
      } text("The json file with the mutants")
      opt[String]('c', "commit") action{ (x,c) =>
        c.copy(commit = x)
      } text("The commit to reference the line number to; default is HEAD")
      opt[String]('f', "out") action { (x,c) =>
        c.copy(file = Some(x))
      } text("The output file")
      opt[Unit]("forward") action { (_,c) =>
        c.copy(forward = true)
      } text("Perform the analysis only on the commits that follow the reference commit, exclusive")
      opt[Unit]("reverse") action { (_, c) =>
        c.copy(reverse = true)
      } text("Perform the analysis only on the commits that preceed the referece commit, inclusive")
    }

    parser.parse(args, Config())
  }

  private def doAnalysis(config: Config): Unit = {
    disableLoggers()

    val finder = if (config.method)
      MethodFinder
    else
      StatementFinder
    val detector = new NodeChangeDetector(config.repo, finder)
    val mutants = config.jsonFile match {
      case Some(x) => JSONDecoder.decode(x)
      case None => Seq()
    }

    val outputStream = config.file match {
      case Some(x) => new PrintStream(new FileOutputStream(new File(x)))
      case None => System.out
    }

    val order = getAnalysisOrder(config)

    val result = mutants.toParArray.map(mutant => {
      mutant.getFileName + "," + mutant.getLineNumber + "," +
        detector.findCommits(mutant.getFileName, mutant.getLineNumber, config.commit, order).map(commit => commit + ",").
          foldRight[String]("")((c, e) => c + e) + "\n"
    }).asParSeq.reduceRight((current, element) => current + element)

    outputStream.write(result.getBytes)
  }

  private[statementHistory] def getAnalysisOrder(config: Config): Order.Value =
    if (config.forward)
      Order.FORWARD
    else if (config.reverse)
      Order.REVERSE
    else
      Order.BOTH

  private def disableLoggers() =
    Matcher.LOGGER.setLevel(Level.OFF)

  def main(args: Array[String]) = {

    val config = parseCmdOptions(args)
    config match {
      case Some(x) =>
        doAnalysis(x)
      case None => println("Incorrect args")
    }

  }
}