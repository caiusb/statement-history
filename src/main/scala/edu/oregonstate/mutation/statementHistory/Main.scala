package edu.oregonstate.mutation.statementHistory

import java.io.{File, FileOutputStream, PrintStream}
import java.util.logging.Level

import com.brindescu.gumtree.facade.Gumtree._
import com.brindescu.gumtree.facade.{CASTDiff, JavaASTDiff, SuperTree}
import com.brindescu.gumtree.jdt.JavaTree
import com.github.gumtreediff.matchers.Matcher
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jgit.api.Git

object Main {

  private[statementHistory] case class Config(method: Boolean = false,
                        block: Boolean = false,
                        repo: File = new File("."),
                        statementFile: Option[File] = None,
                        csvFile: Boolean = false,
                        commit: String = "HEAD",
                        file:Option[String] = None,
                        forward: Boolean = false,
                        reverse: Boolean = false,
                        c: Boolean = false) {
  }

  private[statementHistory] def parseCmdOptions(args: Array[String]): Option[Config] = {
    val parser = new scopt.OptionParser[Config]("java -jar <jar_name>") {
      opt[Boolean]('m', "method") action { (x,c) =>
          c.copy(method = x)
        } text ("track methods; default is false, it tracks statements")
      opt[Boolean]('b', "block") action { (x,c) =>
        c.copy(block = x)
        } text ("track blocks; default is false, it tracks statements")
      opt[String]('r', "repo") required() action { (x,c) =>
        c.copy(repo=new File(x))
      } text("The location of the repository")
      opt[String]('j', "json-file") action { (x,c) =>
        c.copy(statementFile = Some(new File(x)))
      } text("The file with the statemnts, by default, it's a JSON")
      opt[Unit]("csv") action { (_,c) =>
        c.copy(csvFile = true)
      } text("The statement file is in CSV format")
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
      opt[Unit]("c-parser") action { (_, c) =>
        c.copy(c = true)
      } text("Use the C parser instead of the Java one")
    }

    parser.parse(args, Config())
  }

  private def doAnalysis(config: Config): Unit = {
    disableLoggers()

    val finder = if (config.method)
      MethodFinder
    else  if (config.block)
      BlockFinder
    else
      StatementFinder

    if (config.c)
      finder.parser = CParser

    val git = Git.open(config.repo)

    val detector = if (config.c)
      new NodeChangeDetector(git, finder, CASTDiff)
    else
      new NodeChangeDetector(git, finder, JavaASTDiff)


    val decoder = if (config.csvFile)
      CSVDecoder
    else
      JSONDecoder

    val find = (name: String, number: Int) =>
      finder.findNode(git, config.commit, GitUtil.findFullPath(git, config.commit, name), number)
    val statements = config.statementFile match {
      case Some(x) => decoder.decode(x, find)
      case None => getAllNodesInRepo(finder, config, if (config.c) ".c" else ".java")
    }

    val outputStream = config.file match {
      case Some(x) => new PrintStream(new FileOutputStream(new File(x)))
      case None => System.out
    }

    val order = getAnalysisOrder(config)

    val result = statements.toParArray.foreach(statement => {
      val result = statement.printInfo +
        detector.findCommits(statement.getFileName, statement.getLineNumber, config.commit, order).map(commit => commit + ",").
          foldRight[String]("")((c, e) => c + e) + "\n"
      outputStream.write(result.getBytes)
    })
  }

  private[statementHistory] def getAllNodesInRepo(finder: NodeFinder = StatementFinder,
                                                  config: Config,
                                                  extension: String = ".java"): Seq[StatementInfo] = {
    val git = Git.open(config.repo)
    FileFinder.findIn(git, config.commit, extension)
      .foldLeft[Seq[StatementInfo]](Seq())((list, file) => {
      list ++ finder.findAllNodesForFile(git, config.commit, file)
        .map(node => new StatementInfo(file, node))
    })
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