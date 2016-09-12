package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.SuperTree
import org.eclipse.jgit.api.Git

trait NodeFinder {

  var parser: ASTParser = JavaParser

  def findNode(git: Git, commitSHA: String, file: String, lineNumber: Int): SuperTree = {
    val content: String = GitUtil.getFileContent(git, commitSHA, file)
    val ast = parser.parse(content)
    findNode(lineNumber, ast)
  }

  def findNode(lineNumber: Int, astRoot: SuperTree): SuperTree = {
    val statementMap: Map[Int, SuperTree] = getMapOfNodes(astRoot)
    statementMap.get(lineNumber) match {
      case Some(x) => return x
      case None => return null
    }
  }

  def findAllNodesForFile(git: Git, commit: String, file: String): List[SuperTree] = {
    val content = GitUtil.getFileContent(git, commit, file)
    val ast = JavaParser.parse(content)
    getMapOfNodes(ast).values.toList
  }

  def getMapOfNodes(astRoot: SuperTree): Map[Int, SuperTree]
}
