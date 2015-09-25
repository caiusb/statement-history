package edu.oregonstate.mutation.statementHistory

import java.io.File

import org.eclipse.jdt.core.dom.{ASTNode, Statement}
import org.eclipse.jgit.api.Git
import org.gitective.core.BlobUtils

class StatementFinder(repo: String) {

  val git = Git.open(new File(repo))

  def getLines(string: String): Seq[LineInfo] = {
    val lines = string.split("\n")
    var current = 0
    lines.map { line => {
      val lineInfo = new LineInfo(current+1, current+line.length())
      current += line.length()
      lineInfo
    } }
  }

  def findStatement(commitSHA: String, file: String, lineNumber: Int): Statement = {
    val content: String = getFileContent(commitSHA, file)
    val ast: ASTNode = AST.getAST(content)
    findStatement(lineNumber, content, ast)
  }

  def getFileContent(commitSHA: String, file: String): String = {
    BlobUtils.getContent(git.getRepository, commitSHA, file)
  }

  def findStatement(lineNumber: Int, content: String, ast: ASTNode): Statement = {
    val lines = getLines(content)
    val visitor = new StatementVisitor(lines)
    ast.accept(visitor)
    visitor.getStatementMap.get(lineNumber - 1) match {
      case Some(x) => return x
      case None => return null
    }
  }
}