package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.{ASTNode, Statement}
import org.eclipse.jdt.core.dom.AST._
import org.gitective.core.CommitUtils
import org.eclipse.jgit.lib.Repository
import java.io.File
import org.eclipse.jgit.api.Git
import org.gitective.core.BlobUtils
import org.eclipse.core.runtime.NullProgressMonitor

class StatementFinder(repo: String) {

  val git = Git.open(new File(repo))

  def getLines(string: String): Seq[LineInfo] = {
    import scala.collection.mutable._
    val lines = string.split("\n")
    var current = 0
    lines.map { line => {
      val lineInfo = new LineInfo(current+1, current+line.length())
      current += line.length()
      lineInfo
    } }
  }

  def findStatement(commitSHA: String, file: String, lineNumber: Int): Statement = {

    val content = BlobUtils.getContent(git.getRepository, commitSHA, file)
    val ast: ASTNode = getAST(content)
    val lines = getLines(content)
    val visitor = new StatementVisitor(lines)
    ast.accept(visitor)
    visitor.getStatementMap.get(lineNumber-1) match {
      case Some(x) => return x
      case None => return null
    }
  }

  private def getAST(content: String): ASTNode = {
    import org.eclipse.jdt.core.dom.ASTParser._
    val parser = newParser(K_COMPILATION_UNIT)
    parser.setKind(JLS8)
    parser.setSource(content.toCharArray)
    val ast = parser.createAST(new NullProgressMonitor)
    ast
  }
}