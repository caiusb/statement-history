package edu.oregonstate.mutation.statementHistory

class CommitInfo(val sha: String, val action: String) {

  override def toString: String = {
    return "[" + sha + "," + action + "]"
  }

  override def equals(other: Any): Boolean = {
    other match {
      case info: CommitInfo => info.sha == sha && info.action == action
      case _ => false
    }
  }
}
