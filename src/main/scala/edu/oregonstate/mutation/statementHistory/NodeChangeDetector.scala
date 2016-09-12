package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.Gumtree._
import com.brindescu.gumtree.facade.{Diff, JavaASTDiff, SuperTree}
import com.brindescu.gumtree.jdt.JavaTree
import com.github.gumtreediff.actions.model._
import edu.oregonstate.mutation.statementHistory.Order._
import org.eclipse.jdt.core.dom.ASTNode
import org.eclipse.jgit.api.Git

class NodeChangeDetector(private val git: Git, private val finder: NodeFinder) {

  private implicit def st(s: SuperTree): ASTNode = s.asInstanceOf[JavaTree]

  def findCommits(filePath: String, lineNo: Int, commit: String = "HEAD", order: Value = BOTH): Seq[CommitInfo] = {
    val finder = new CommitFinder(git)
    val fullPath = GitUtil.findFullPath(git, commit, filePath)
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

  def process(lineNo: Int, fullPath: String, commits: Seq[String], forwardInTime: Boolean): ChangeInfo = {
    val lastBit = commits.sliding(2).foldLeft(new ChangeInfo(lineNo, List())) { (change, pair) =>
      processPair(pair, fullPath, change.getLine, forwardInTime) match {
        case Some(x) => change.merge(x)
        case None => change
      }
    }
    lastBit
  }

  private def processPair(pair: Seq[String], path: String, line: Int, forwardInTime: Boolean): Option[ChangeInfo] = {
    if (pair.size != 2)
      return None

    val (oldCommit, newCommit) = if (forwardInTime) (pair(0), pair(1)) else (pair(1), pair(0))

    val diff = JavaASTDiff.getDiff(GitUtil.getFileContent(git, oldCommit, path), GitUtil.getFileContent(git, newCommit, path))

    val oldTree = diff.getLeftTree()
    val newTree = diff.getRightTree()

    val node = if (forwardInTime)
      finder.findNode(line, oldTree.getASTNode)
    else
      finder.findNode(line, newTree.getASTNode)

    val actions = diff.getActions

    val nextLine = getNextLine(node, diff, forwardInTime)
    val matchingActions = processActions(actions, diff, forwardInTime)
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
    return Some(new ChangeInfo(nextLine, Seq()))
  }

  def processActions(actions: Seq[Action], diff: Diff, forwardInTime: Boolean): Seq[Action] = {
    if (!forwardInTime)
      actions.map(action => action match {
        case m: Update => new Update(diff.getMatch(m.getNode).get, "0")
        case m: Move =>
          val node = diff.getMatch(m.getNode).get
          new Move(node, node.getParent, 1)
        case m => m
      })
    else
      actions
  }

  private def getActionsTouchingNode(statement: SuperTree, actions: Seq[Action]): Seq[Action] = {
    actions.filter(action => {
      val node = action.getNode.getASTNode
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

  private def getNextLine(astNode: SuperTree, diff: Diff, forwardInTime: Boolean): Int = {
    diff.getMatchedNodes.map{ case t => (t._1.getASTNode, t._2.getASTNode)}.foreach(
        _ match {
          case (x, s) if s == astNode && !forwardInTime =>
            return x.getLineNumber()
          case (s, x) if s == astNode && forwardInTime =>
            return x.getLineNumber()
          case _ => ;
        })
    return -1
  }

  def isInNode(node: ASTNode, target: ASTNode): Boolean = {
    if (node == null)
      return false
    if (node.equals(target))
      return true
    isInNode(node.getParent, target)
  }
}