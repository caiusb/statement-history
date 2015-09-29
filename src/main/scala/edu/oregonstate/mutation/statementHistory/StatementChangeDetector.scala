package edu.oregonstate.mutation.statementHistory

import java.io.File

import fr.labri.gumtree.actions.model._
import fr.labri.gumtree.gen.jdt.JdtTree
import fr.labri.gumtree.matchers.MappingStore
import org.eclipse.jdt.core.dom.{ASTNode, CompilationUnit, Statement}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.gitective.core.CommitUtils

import scala.collection.JavaConversions

class StatementChangeDetector(repo: String, sha: String) {

  var git = Git.open(new File((repo)))

  def findCommits(filePath: String, lineNo: Int): Seq[CommitInfo] = {
    var line = lineNo
    var validCommits = scala.collection.mutable.Seq[CommitInfo]()
    val commitsOfFile = new FileFinder(repo).findAll(filePath, sha)
    val firstCommit = commitsOfFile.last
    var fullPath = findFullPath(CommitUtils.getCommit(git.getRepository, firstCommit), filePath)
    var statement = new StatementFinder(repo).findStatement(firstCommit, fullPath, line)

    val finder = new StatementFinder(repo)

    val last = commitsOfFile.reverse.reduce((newer, older) => {
      if (line == -1)  //TODO: I do not like this hack. I need fo find a nicer way to solve this
        return validCommits

      val diff = new ASTDiff
      val newerContent = finder.getFileContent(newer, fullPath)
      val newerTree = diff.getTree(newerContent)
      val statement = finder.findStatement(line, newerContent, newerTree.asInstanceOf[JdtTree].getContainedNode)
      val olderTree = diff.getTree(finder.getFileContent(older, fullPath))
      val (actions, matchings) = diff.getActions(olderTree, newerTree)
      val changedStatement = actions.find(action => {
        val node = matchings.getDst(action.getNode).asInstanceOf[JdtTree].getContainedNode
        isInStatement(statement, node)
      })
      changedStatement match {
        case Some(x) =>
          x match {
            case _: Delete => validCommits = validCommits :+ new CommitInfo(newer, "DELETE")
              line = -1 // Stop tracking
            case _: Update => validCommits = validCommits :+ new CommitInfo(newer, "UPDATE")
              line = findOldLine(statement, matchings)
            case _: Move => validCommits = validCommits :+ new CommitInfo(newer, "MOVE")
              line = findOldLine(statement, matchings)
            case _ => ;
          }
        case _ => ;
      }
      older
    })

    validCommits.reverse.+:(new CommitInfo(last, "ADD"))
  }

  def findOldLine(statement: Statement, matchings: MappingStore): Int = {
    JavaConversions.asScalaIterator(matchings.iterator()).find(m => {
      m.getSecond.asInstanceOf[JdtTree].getContainedNode == statement
    }) match {
      case Some(m) => val firstNode = m.getFirst.asInstanceOf[JdtTree].getContainedNode
        val start = firstNode.getStartPosition
        firstNode.getRoot.asInstanceOf[CompilationUnit].getLineNumber(start)
      case _ => -1
    }
  }

  private def isInStatement(stmt: Statement, node: ASTNode): Boolean = {
    if (node == null)
      return false
    if (node.equals(stmt))
      return true

    isInStatement(stmt, node.getParent)
  }

  private def findFullPath(commit: RevCommit, path: String): String = {
    var diffs = GitUtil.getDiffs(git, commit)
    diffs.filter(diff => {
      diff.getNewPath.endsWith(path)
    })(0).getNewPath
  }

}