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
    findNode(lineNumber, ast)
  }

  def findNode(lineNumber: Int, astRoot: ASTNode): ASTNode = {
    val statementMap: Map[Int, ASTNode] = getMapOfNodes(astRoot)
    statementMap.get(lineNumber - 1) match {
      case Some(x) => return x
      case None => return null
    }
  }

  def findAllNodesForFile(git: Git, commit: String, file: String): List[ASTNode] = {
    val content = GitUtil.getFileContent(git, commit, file)
    val ast = AST.getAST(content)
    getMapOfNodes(ast).values.toList
  }

  def getMapOfNodes(astRoot: ASTNode): Map[Int, ASTNode]
}
