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

class StatementChangeDetector(repo: File, sha: String) {

  var git = Git.open(repo)

  def this(repo: String, sha: String) = this(new File(repo), sha)

  def findCommits(filePath: String, lineNo: Int): Seq[CommitInfo] =
    findCommits(filePath, lineNo, "HEAD")

  def findCommits(filePath: String, lineNo: Int, commit: String): Seq[CommitInfo] = {
    var line = lineNo
    var validCommits = scala.collection.mutable.Seq[CommitInfo]()
    val commitsOfFile = new FileFinder(repo.getAbsolutePath).findAll(filePath, sha)
    val fullPath = findFullPath(CommitUtils.getCommit(git.getRepository, commit), filePath)
    val statement = new StatementFinder(repo.getAbsolutePath).findStatement(commit, fullPath, line)

    val finder = new StatementFinder(repo.getAbsolutePath)

    commitsOfFile.reverse.sliding(2).foreach(l => {
      val newer = l(0)
      val older = l(1)

      if (line == -1)
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

      if (relevantActions.exists(isUpdate))
        relevantActions = relevantActions.filter(isUpdate)
      else
        relevantActions = relevantActions.filter(action => {
          action.getNode.asInstanceOf[JdtTree].getContainedNode == statement})

      val first = relevantActions.foreach(changedStatement =>
        changedStatement match {
          case _: Insert => validCommits = validCommits.+:(new CommitInfo(newer, "ADD"))
          case _: Update => validCommits = validCommits.+:(new CommitInfo(newer, "UPDATE"))
          case _: Move => validCommits = validCommits.+:(new CommitInfo(newer, "MOVE"))
          case _ => ;
        })
      line = oldLine
    })

   if (line != -1)
      validCommits.+:(new CommitInfo(commitsOfFile(0), "ADD"))
    else
      validCommits
  }

  private def trackStatement(older: String, newer: String): String = {
    return ""
  }

  def isUpdate: (Action) => Boolean =
    action => action match {
      case _: Update => true
      case _ => false
    }

  def convertMatching(matchings: MappingStore): List[(ASTNode, ASTNode)] = {
    var list = List[(ASTNode, ASTNode)]()
    JavaConversions.asScalaIterator(matchings.iterator()).foreach(m => {
      val first = m.getFirst.asInstanceOf[JdtTree].getContainedNode
      val second = m.getSecond.asInstanceOf[JdtTree].getContainedNode
      list = list.+:(first,second)
    })
    list
  }

  def findOldLine(statement: Statement, matchings: MappingStore): Int =
    findOldLine(statement, convertMatching(matchings))

  def findOldLine(statement: Statement, m: List[(ASTNode, ASTNode)]): Int =
    m match {
      case first :: rest =>
        first match {
          case (x, s) if (s == statement) =>
            getLineNumber(x)
          case _ => findOldLine(statement, rest)
        }
      case Nil => -1
    }

  private def getLineNumber(node: ASTNode): Int =
    node.getRoot.asInstanceOf[CompilationUnit].getLineNumber(node.getStartPosition)

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