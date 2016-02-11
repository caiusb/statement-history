package edu.oregonstate.mutation.statementHistory

import edu.oregonstate.mutation.statementHistory.Order._
import fr.labri.gumtree.actions.model._
import fr.labri.gumtree.gen.jdt.JdtTree
import fr.labri.gumtree.matchers.MappingStore
import org.eclipse.jdt.core.dom.{ASTNode, CompilationUnit}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter

import scala.collection.JavaConversions

class NodeChangeDetector(private val git: Git, private val finder: NodeFinder) {

  def findCommits(filePath: String, lineNo: Int, commit: String = "HEAD", order: Value = BOTH): Seq[CommitInfo] = {
    val finder = new CommitFinder(git)
    val fullPath = findFullPath(GitUtil.getCommit(git, commit), filePath)
    val before = finder.findAllCommits(fullPath, commit)
    val after = finder.findAllCommits(fullPath, "HEAD").filter(c => !before.contains(c)).+:(before.last)

    val beforeChanges = if (order == REVERSE || order == BOTH) {
      val lastBit = process(lineNo, fullPath, before.reverse, false)

      if (lastBit.getLine != -1)
        lastBit.getChangedCommits.reverse.+:(new CommitInfo(before(0), "ADD"))
      else
        lastBit.getChangedCommits.reverse
    } else
      Seq()

    val afterChanges = if (order == FORWARD || order == BOTH)
      process(lineNo, fullPath, after, true).getChangedCommits
    else
      Seq()

    beforeChanges ++ afterChanges
  }

  def process(lineNo: Int, fullPath: String, commits: Seq[String], isReversed: Boolean): ChangeInfo = {
    val lastBit = commits.sliding(2).foldLeft(new ChangeInfo(lineNo, List())) { (change, pair) =>
      processPair(pair, fullPath, change.getLine, isReversed) match {
        case Some(x) => change.merge(x)
        case None => change
      }
    }
    lastBit
  }

  private def processPair(pair: Seq[String], path: String, line: Int, propagateForward: Boolean): Option[ChangeInfo] = {
    if (pair.size != 2)
    return None

    val oldCommit = if (propagateForward) pair(0) else pair(1)
    val newCommit = if (propagateForward) pair(1) else pair(0)

    val oldTree = ASTDiff.getTree(GitUtil.getFileContent(git, oldCommit, path))
    val newTree = ASTDiff.getTree(GitUtil.getFileContent(git, newCommit, path))
    val node = if (propagateForward)
    finder.findNode(line, oldTree.asInstanceOf[JdtTree].getContainedNode)
    else
    finder.findNode(line, newTree.asInstanceOf[JdtTree].getContainedNode)
    val (actions, matchings) = ASTDiff.getActions(oldTree, newTree)

    val nextLine = getNextLine(node, matchings, propagateForward)
    var matchingActions = processActions(actions, matchings, propagateForward)
    val statementActions = getActionsTouchingNode(node, matchingActions)
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
    val tree = commit.getTree
    val walk = new TreeWalk(git.getRepository)
    walk.addTree(tree)
    walk.setRecursive(true)
    walk.setFilter(PathSuffixFilter.create(path))
    walk.next()
    walk.getPathString
  }
}