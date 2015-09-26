package edu.oregonstate.mutation.statementHistory

import java.io.File
import java.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.{RevCommit, RevSort, RevWalk}
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import org.gitective.core.CommitUtils

class FileFinder(repo: String) {

	val git = Git.open(new File(repo))

  def createWalkWithFilter(path: String) = {
    val walk = new RevWalk(git.getRepository)
    val treeFilter = PathSuffixFilter.create(path)
    walk.setTreeFilter(treeFilter)
    walk.sort(RevSort.NONE) //clear filters
    walk.sort(RevSort.COMMIT_TIME_DESC, true)
    walk.sort(RevSort.REVERSE, true)
    walk.markStart(CommitUtils.getCommit(git.getRepository, "HEAD"))
    walk
  }

	def findFirst(path: String): RevCommit = {
			val walk = createWalkWithFilter(path)
			val nextCommit = walk.next
			walk.close
			nextCommit
	}

  def findAll(path: String): Seq[String] = {
    import GitUtil._
    import scala.collection.JavaConversions._

    val first = findFirst(path)
    val walk = new RevWalk(git.getRepository)
    walk.sort(RevSort.NONE)
    walk.sort(RevSort.COMMIT_TIME_DESC, true)
    walk.sort(RevSort.REVERSE, true)
    walk.markStart(CommitUtils.getCommit(git.getRepository, "HEAD"))
    walk.markUninteresting(first)
    
    val iterator = asScalaIterator(walk.iterator)

    def ifCommitChangesFile(commit: RevCommit) = {
      val diffs: util.List[DiffEntry] = getDiffs(git, commit)
      val result = diffs.filter { diff => diff.getNewPath.endsWith(path) || diff.getOldPath.endsWith(path) }
      result.size != 0
    }
    
    val shas = iterator.filter(ifCommitChangesFile).map { commit => commit.getName }
    
    return shas.toSeq
	}


}