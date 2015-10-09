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

  private var git = Git.open(repo)
  private val finder = new StatementFinder(repo.getAbsolutePath)

  def this(repo: String, sha: String) = this(new File(repo), sha)

  def findCommits(filePath: String, lineNo: Int): Seq[CommitInfo] =
    findCommits(filePath, lineNo, "HEAD")
  def findCommits(filePath: String, lineNo: Int, commit: String): Seq[CommitInfo] = {
    val before = new FileFinder(repo.getAbsolutePath).findAll(filePath, sha)
    val after = new FileFinder(repo.getAbsolutePath).findAll(filePath, "HEAD").filter(commit => {
      !before.contains(commit)
    })

    val fullPath = findFullPath(CommitUtils.getCommit(git.getRepository, commit), filePath)

    val beforeResults = before.reverse.sliding(2).foldLeft(new ChangeInfo(lineNo, Seq())){(c, l) => processPair(c, l, fullPath, findOldLine)}

    if (beforeResults.getLine != -1)
      beforeResults.copy(-1, new CommitInfo(before(0), "ADD")).getChangedCommits.reverse
    else
      beforeResults.getChangedCommits.reverse
  }


  private def processPair(c: ChangeInfo,
                          l: Seq[String],
                          fullPath: String,
                          getNextLine:(Statement, MappingStore) => Int): ChangeInfo = {
    val newer = l(0)
    val older = l(1)

    if (c.getLine == -1)
      return c

    val diff = new ASTDiff
    val newerContent = finder.getFileContent(newer, fullPath)
    val newerTree = diff.getTree(newerContent)
    val statement = finder.findStatement(c.getLine, newerContent, newerTree.asInstanceOf[JdtTree].getContainedNode)
    val olderTree = diff.getTree(finder.getFileContent(older, fullPath))
    val (actions, matchings) = diff.getActions(olderTree, newerTree)
    val nextLine = getNextLine(statement, matchings)
    val relevantActions = actions.filter(action => {
      action match {
        case _: Insert => nextLine == -1
        case _: Delete => false
        case _ =>
          val node = matchings.getDst(action.getNode).asInstanceOf[JdtTree].getContainedNode
          isInStatement(statement, node)
      }
    })

    if(!relevantActions.contains(isUpdate))
      relevantActions.withFilter(action => {
        action.getNode.asInstanceOf[JdtTree].getContainedNode == statement
      })
    else
      relevantActions.withFilter(isUpdate)

    relevantActions.foldLeft(c)((c, changedStatement) =>
      changedStatement match {
        case _: Insert => c.copy(nextLine, new CommitInfo(newer, "ADD"))
        case _: Update => c.copy(nextLine, new CommitInfo(newer, "UPDATE"))
        case _: Move => c.copy(nextLine, new CommitInfo(newer, "MOVE"))
        case _ => c.copy(nextLine);
      })
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


  def findNewLine(statement: Statement, matchings: MappingStore): Int =
    findNewLine(statement, convertMatching(matchings))

  def findNewLine(statement: Statement, m: List[(ASTNode, ASTNode)]): Int =
    m match {
      case first :: rest =>
        first match {
          case (s, x) if (s == statement) =>
            getLineNumber(x)
          case _ => findNewLine(statement, rest)
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