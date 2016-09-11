package edu.oregonstate.mutation.statementHistory

protected class ChangeInfo(private var nextLine: Int,
                 private var commitInfos: Seq[CommitInfo]) {

  def merge(info: ChangeInfo): ChangeInfo = {
    new ChangeInfo(info.getLine, commitInfos ++ info.getChangedCommits)
  }

  def getChangedCommits: Seq[CommitInfo] = commitInfos

  def getLine: Int = nextLine

}
