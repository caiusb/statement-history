package edu.oregonstate.mutation.statementHistory

import java.io.File

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.gitective.core.CommitUtils

class FileFinder(repo: String) {
  
	val git = Git.open(new File(repo))

  def createWalkWithFilter(path: String) = {
    val walk = new RevWalk(git.getRepository)
    val treeFilter = PathFilter.create(path)
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
    import scala.collection.JavaConversions._;
    
    val first = findFirst(path)
    val walk = new RevWalk(git.getRepository)
    walk.sort(RevSort.NONE)
    walk.sort(RevSort.COMMIT_TIME_DESC, true)
    walk.sort(RevSort.REVERSE, true)
    walk.markStart(CommitUtils.getCommit(git.getRepository, "HEAD"))
    walk.markUninteresting(first)
    
    val iterator = asScalaIterator(walk.iterator)
    
    def ifCommitChangesFile(commit: RevCommit) = {
      val diff = new DiffFormatter(DisabledOutputStream.INSTANCE)
      diff.setRepository(git.getRepository)
      diff.setDiffComparator(RawTextComparator.DEFAULT)
      diff.setDetectRenames(true)
      var secondTree:RevTree = null
      if (commit.getParentCount != 0)
        secondTree = commit.getParent(0).getTree
      val diffs = diff.scan(secondTree, commit.getTree)
      val result = diffs.filter { diff => diff.getNewPath.equals(path) || diff.getOldPath.equals(path) }
      result.size != 0
    }
    
    val shas = iterator.filter(ifCommitChangesFile).map { commit => commit.getName }
    
    return shas.toSeq
	}
}