package edu.oregonstate.mutation.statementHistory

import java.io.File

import org.eclipse.jdt.core.dom.{ASTNode, Statement}
import org.eclipse.jgit.api.Git
import org.gitective.core.BlobUtils

class StatementFinder(repo: String) {

  val git = Git.open(new File(repo))

  def findStatement(commitSHA: String, file: String, lineNumber: Int): Statement = {
    val content: String = GitUtil.getFileContent(git, commitSHA, file)
    val ast: ASTNode = AST.getAST(content)
    findStatement(lineNumber, content, ast)
  }

  def findStatement(lineNumber: Int, content: String, ast: ASTNode): Statement = {
    val visitor = new StatementVisitor()
    ast.accept(visitor)
    visitor.getStatementMap.get(lineNumber - 1) match {
      case Some(x) => return x
      case None => return null
    }
  }
}