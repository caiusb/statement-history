package edu.oregonstate.mutation.statementHistory

/**
 * Created by caius on 9/28/15.
 */
class CommitInfo(val sha: String, val action: String) {

  override def toString: String = {
    return sha + "," + action
  }

  override def equals(other: Any): Boolean = {
    other match {
      case info: CommitInfo => info.sha == sha && info.action == action
      case _ => false
    }
  }
}
