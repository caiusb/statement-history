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

class NodeChangeDetector(private val repo: File, private val finder: NodeFinder) {

  private val git = Git.open(repo)

  def this(repo: String, finder: NodeFinder) = this(new File(repo), finder)

  def findCommits(filePath: String, lineNo: Int, commit: String = "HEAD"): Seq[CommitInfo] = {
    val finder = new FileFinder(repo.getAbsolutePath)
    val fullPath = findFullPath(GitUtil.getCommit(git, commit), filePath)
    val before = finder.findAll(fullPath, commit)
    val after = finder.findAll(fullPath, "HEAD").filter(c => !before.contains(c)).+:(before.last)

    val lastBit = before.reverse.sliding(2).foldLeft(new ChangeInfo(lineNo, List())){ (change, pair) =>
      processPair(pair, fullPath, change.getLine, false) match {
        case Some(x) => change.merge(x)
        case None => change
      }
    }
    val afterChanges = after.sliding(2).foldLeft(new ChangeInfo(lineNo, List())){ (change, pair) =>
      processPair(pair, fullPath, change.getLine, true) match {
        case Some(x) => change.merge(x)
        case None => change
      }
    }.getChangedCommits

    val beforeChanges = if (lastBit.getLine != -1)
      lastBit.getChangedCommits.reverse.+:(new CommitInfo(before(0), "ADD"))
    else
      lastBit.getChangedCommits.reverse

    beforeChanges ++ afterChanges
  }

  private def processPair(pair: Seq[String], path: String, line: Int, propagateForward: Boolean): Option[ChangeInfo] = {
    if (pair.size != 2)
    return None

    val oldCommit = if (propagateForward) pair(0) else pair(1)
    val newCommit = if (propagateForward) pair(1) else pair(0)

    val astDiff = new ASTDiff
    val oldTree = astDiff.getTree(GitUtil.getFileContent(git, oldCommit, path))
    val newTree = astDiff.getTree(GitUtil.getFileContent(git, newCommit, path))
    val statement = if (propagateForward)
    finder.findNode(line, oldTree.asInstanceOf[JdtTree].getContainedNode)
    else
    finder.findNode(line, newTree.asInstanceOf[JdtTree].getContainedNode)
    val (actions, matchings) = astDiff.getActions(oldTree, newTree)

    val nextLine = getNextLine(statement, matchings, propagateForward)
    var matchingActions = processActions(actions, matchings, propagateForward)
    val statementActions = getActionsTouchingNode(statement, matchingActions)
    statementActions.foreach(action => {
      action match {
        case _: Update => return Some(new ChangeInfo(nextLine, Seq(new CommitInfo(newCommit, "UPDATE"))))
        case _: Move => return Some(new ChangeInfo(nextLine, Seq(new CommitInfo(newCommit, "MOVE"))))
        case _: Insert if nextLine == -1 => return Some(new ChangeInfo(nextLine, Seq(new CommitInfo(newCommit, "ADD"))))
        case _: Delete if nextLine == -1 => return Some(new ChangeInfo(nextLine, Seq(new CommitInfo(newCommit, "DELETE"))))
        case _ => ;
      }
    })
    return None
  }

  def processActions(actions: Seq[Action], matchings: MappingStore, propagateForward: Boolean): Seq[Action] = {
    if (!propagateForward)
      actions.map(action => action match {
        case m: Update => new Update(matchings.getDst(m.getNode), "0")
        case m: Move =>
          val node = matchings.getDst(m.getNode)
          new Move(node, node.getParent, 1)
        case m => m
      })
    else
      actions
  }

  private def getActionsTouchingNode(statement: ASTNode, actions: Seq[Action]): Seq[Action] = {
    actions.filter(action => {
      val node = action.getNode.asInstanceOf[JdtTree].getContainedNode
      isInNode(node, statement)
    }).sortWith((first, second) => {
      second match {
        case _: Update => first match {
          case _: Insert => false
          case _ => true
        }
        case _ => true
      }
    })
  }

  private def getNextLine(astNode: ASTNode, matchings: MappingStore, followForward: Boolean): Int = {
    convertToTuples(matchings).foreach(tupple => {
      if (followForward)
        tupple match {
          case (s, x) if s == astNode =>
            return x.getRoot.asInstanceOf[CompilationUnit].getLineNumber(x.getStartPosition)
          case _ => ;
        }
      else
        tupple match {
          case (x, s) if s == astNode =>
            return x.getRoot.asInstanceOf[CompilationUnit].getLineNumber(x.getStartPosition)
          case _ => ;
        }
    })
    return -1
  }

  private def convertToTuples(matchings: MappingStore): List[(ASTNode, ASTNode)] = {
    JavaConversions.asScalaIterator(matchings.iterator).map(matching => {
      (matching.getFirst.asInstanceOf[JdtTree].getContainedNode,
        matching.getSecond.asInstanceOf[JdtTree].getContainedNode)
    }).toList
  }

  def isInNode(node: ASTNode, target: ASTNode): Boolean = {
    if (node == null)
      return false
    if (node.equals(target))
      return true
    isInNode(node.getParent, target)
  }

  private def findFullPath(commit: RevCommit, path: String): String = {
    val diffs = GitUtil.getDiffs(git, commit)
    diffs.filter(diff => {
      diff.getNewPath.endsWith(path)
    })(0).getNewPath
  }
}