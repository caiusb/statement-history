package edu.oregonstate.mutation.statementHistory

import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by caius on 10/14/15.
 */
class CommitInfoTest extends FlatSpec with Matchers {

  it should "correctly check for equality" in {
    new CommitInfo("abced", "ADD").equals(new CommitInfo("abced", "ADD")) should be (true)
    new CommitInfo("abced", "ADD").equals(new CommitInfo("adced", "UPDATE")) should be (false)
    new CommitInfo("abced", "ADD").equals(Seq()) should be (false)
  }
}
