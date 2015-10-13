package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jgit.api.Git

/**
 * Created by caius on 10/12/15.
 */
trait NodeFinder {

  def findNode(git: Git, commitSHA: String, file: String, lineNumber: Int): ASTNode = {
    val content: String = GitUtil.getFileContent(git, commitSHA, file)
    val ast: ASTNode = AST.getAST(content)
    findNode(git, lineNumber, ast)
  }

  def findNode(git: Git, line: Int, root: ASTNode): ASTNode

}
