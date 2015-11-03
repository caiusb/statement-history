package edu.oregonstate.mutation.statementHistory

import java.io.File
import java.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevSort._
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import org.gitective.core.CommitUtils

class CommitFinder(private val git: Git) {

  def createWalkWithFilter(path: String, sha: String) = {
    val walk = new RevWalk(git.getRepository)
    val treeFilter = PathSuffixFilter.create(path)
    walk.setTreeFilter(treeFilter)
    walk.sort(NONE) //clear filters
    walk.sort(COMMIT_TIME_DESC, true)
    walk.sort(REVERSE, true)
    walk.markStart(CommitUtils.getCommit(git.getRepository, sha))
    walk
  }

	def findFirstCommit(path: String, sha: String): RevCommit = {
			val walk = createWalkWithFilter(path, sha)
			val nextCommit = walk.next
			walk.close
			nextCommit
	}

  def findAllCommits(path: String, sha: String): Seq[String] = {
    import GitUtil._

    import scala.collection.JavaConversions._

    val first = findFirstCommit(path, sha)
    val walk = new RevWalk(git.getRepository)
    walk.sort(NONE)
    walk.sort(COMMIT_TIME_DESC, true)
    walk.sort(REVERSE, true)
    walk.markStart(CommitUtils.getCommit(git.getRepository, sha))
    walk.markUninteresting(first)
    
    val iterator = asScalaIterator(walk.iterator)

    def ifCommitChangesFile(commit: RevCommit) = {
      val diffs: util.List[DiffEntry] = getDiffs(git, commit)
      val result = diffs.filter { diff => diff.getNewPath.endsWith(path) || diff.getOldPath.endsWith(path) }
      result.size != 0
    }
    
    iterator.filter(ifCommitChangesFile).map { commit => commit.getName }.toSeq
	}


}