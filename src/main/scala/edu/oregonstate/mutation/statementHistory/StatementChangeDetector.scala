package edu.oregonstate.mutation.statementHistory

import java.io.File

import fr.labri.gumtree.gen.jdt.JdtTree
import org.eclipse.jdt.core.dom.{Statement, ASTNode}
import org.eclipse.jgit.api.Git

class StatementChangeDetector(repo: String) {

  var git = Git.open(new File((repo)))

  def findCommits(filePath: String, lineNo: Int): Seq[String] = {
    var validCommits = scala.collection.mutable.Seq[String]()
    val commitsOfFile = new FileFinder(repo).findAll(filePath)
    val firstCommit = commitsOfFile(0)
    val statement = new StatementFinder(repo).findStatement(firstCommit, filePath, lineNo)

    validCommits = validCommits :+ firstCommit

    val finder = new StatementFinder(repo)

    commitsOfFile.reduce((left, right) => {
      val diff = new ASTDiff
      val leftContent = finder.getFileContent(left, filePath)
      val leftTree = diff.getTree(leftContent)
      val leftStatement = finder.findStatement(lineNo, leftContent, leftTree.asInstanceOf[JdtTree].getContainedNode)
      val actions = diff.getActions(leftTree, diff.getTree(finder.getFileContent(right, filePath)))
      if (actions.exists(action => {
        val node = action.getNode.asInstanceOf[JdtTree].getContainedNode
        isInStatement(leftStatement, node)
      })) {
        validCommits = validCommits :+ right
      }
      right
    })

    return validCommits
  }

  private def isInStatement(stmt: Statement, node: ASTNode): Boolean = {
    if (node.equals(stmt))
      return true

    isInStatement(stmt, node.getParent)
  }

}