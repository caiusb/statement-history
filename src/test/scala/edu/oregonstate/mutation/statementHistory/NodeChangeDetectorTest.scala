package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git

/**
 * Created by caius on 12/6/15.
 */
trait NodeChangeDetectorTest {

  def ci(a: String, b:String): CommitInfo = new CommitInfo(a, b)
  def nd(git: Git): NodeChangeDetector = nd(git, StatementFinder)
  def nd(git: Git, finder: NodeFinder): NodeChangeDetector = new NodeChangeDetector(git, finder)

}
