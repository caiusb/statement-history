package edu.oregonstate.mutation.statementHistory

import java.io.File

import fr.labri.gumtree.actions.model.{Update, Delete}
import fr.labri.gumtree.gen.jdt.JdtTree
import org.eclipse.jdt.core.dom.{CompilationUnit, Statement, ASTNode}
import org.eclipse.jgit.api.Git

class StatementChangeDetector(repo: String) {

  var git = Git.open(new File((repo)))

  def findCommits(filePath: String, lineNo: Int): Seq[String] = {
    var line = lineNo
    var validCommits = scala.collection.mutable.Seq[String]()
    val commitsOfFile = new FileFinder(repo).findAll(filePath)
    val firstCommit = commitsOfFile(0)
    var statement = new StatementFinder(repo).findStatement(firstCommit, filePath, line)

    validCommits = validCommits :+ firstCommit

    val finder = new StatementFinder(repo)

    commitsOfFile.reduce((left, right) => {
      if (line == -1)  //TODO: I do not like this hack. I need fo find a nicer way to solve this
        return validCommits

      val diff = new ASTDiff
      val leftContent = finder.getFileContent(left, filePath)
      val leftTree = diff.getTree(leftContent)
      val leftStatement = finder.findStatement(line, leftContent, leftTree.asInstanceOf[JdtTree].getContainedNode)
      val (actions, matchings) = diff.getActions(leftTree, diff.getTree(finder.getFileContent(right, filePath)))
      val changedStatement = actions.find(action => {
        val node = action.getNode.asInstanceOf[JdtTree].getContainedNode
        isInStatement(leftStatement, node)
      })
      changedStatement match {
        case Some(x) => validCommits = validCommits :+ right
          x match {
            case x: Delete => line = -1 // Stop tracking
            case x: Update => val node = x.getNode.asInstanceOf[JdtTree]
              val jdtNode = matchings.getDst(node).asInstanceOf[JdtTree].getContainedNode
              val startPosition = jdtNode.getStartPosition
              line = jdtNode.getRoot.asInstanceOf[CompilationUnit].getLineNumber(startPosition)
          }
        case _ => ;
      }
      right
    })

    return validCommits
  }

  private def isInStatement(stmt: Statement, node: ASTNode): Boolean = {
    if (node == null)
      return false
    if (node.equals(stmt))
      return true

    isInStatement(stmt, node.getParent)
  }

}