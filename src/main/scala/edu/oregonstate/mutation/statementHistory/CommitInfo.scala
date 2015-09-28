package edu.oregonstate.mutation.statementHistory

/**
 * Created by caius on 9/28/15.
 */
class CommitInfo(sha: String, action: String) {

  override def toString: String = {
    return sha + "," + action
  }
}
