package edu.oregonstate.mutation.statementHistory

/**
 * Created by caius on 10/9/15.
 */
protected class ChangeInfo(private var nextLine: Int,
                 private var commitInfos: Seq[CommitInfo]) {

  def copy(nextLine: Int, newInfo: CommitInfo): ChangeInfo = {
    if (!commitInfos.contains(newInfo))
      new ChangeInfo(nextLine, commitInfos.:+(newInfo))
    else
     new ChangeInfo(nextLine, commitInfos)
  }

  def copy(nextLine: Int) = {
    new ChangeInfo(nextLine, commitInfos)
  }

  def getChangedCommits: Seq[CommitInfo] = commitInfos

  def getLine: Int = nextLine

}
