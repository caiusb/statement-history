package edu.oregonstate.mutation.statementHistory

import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by caius on 10/14/15.
 */
class ChangeInfoTest extends FlatSpec with Matchers {

  it should "merge correctly" in {
    val firstCommitInfo = new CommitInfo("abc", "ADD")
    val secondCommitInfo = new CommitInfo("def", "UPDATE")
    val info = new ChangeInfo(17, Seq(firstCommitInfo))
    val add = new ChangeInfo(27, Seq(secondCommitInfo))
    val merged = info.merge(add)
    merged.getLine should equal (27)
    merged.getChangedCommits should equal (Seq(firstCommitInfo ,secondCommitInfo))
  }
}
