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

    val last = commitsOfFile.reduceRight((older, newer) => {
      if (line == -1)  //TODO: I do not like this hack. I need fo find a nicer way to solve this
        return validCommits

      val diff = new ASTDiff
      val newerContent = finder.getFileContent(newer, fullPath)
      val newerTree = diff.getTree(newerContent)
      val statement = finder.findStatement(line, newerContent, newerTree.asInstanceOf[JdtTree].getContainedNode)
      val olderTree = diff.getTree(finder.getFileContent(older, fullPath))
      val (actions, matchings) = diff.getActions(olderTree, newerTree)
      val oldLine = findOldLine(statement, matchings)
      var relevantActions = actions.filter(action => {
        action match {
          case _: Insert => oldLine == -1
          case _: Delete => false
          case _ =>
            val node = matchings.getDst(action.getNode).asInstanceOf[JdtTree].getContainedNode
            isInStatement(statement, node)
        }
      })

      println("Looking for: " + statement + " at " + line)
      if (relevantActions.exists(isUpdate))
        relevantActions = relevantActions.filter(isUpdate)
      else
        relevantActions = relevantActions.filter(action => {
          println(action.getNode.asInstanceOf[JdtTree].getContainedNode.getClass)
          action.getNode.asInstanceOf[JdtTree].getContainedNode == statement})

      relevantActions.foreach(changedStatement =>
        changedStatement match {
          case _: Insert => validCommits = validCommits :+ new CommitInfo(newer, "ADD")
          case _: Update => validCommits = validCommits :+ new CommitInfo(newer, "UPDATE")
          case _: Move => validCommits = validCommits :+ new CommitInfo(newer, "MOVE")
          case _ => ;
      })
      line = oldLine
      older
    })

    if (line != -1)
      validCommits.reverse.+:(new CommitInfo(last, "ADD"))
    else
      validCommits.reverse
  }

  def isUpdate: (Action) => Boolean = {
    action => action match {
      case _: Update => true
      case _ => false
    }
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