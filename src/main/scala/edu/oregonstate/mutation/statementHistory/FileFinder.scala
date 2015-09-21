package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git
import java.io.File
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.eclipse.jgit.revwalk.RevSort
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

  def findFirst(path: String): String = {
    val walk = createWalkWithFilter(path)
    val nextCommit = walk.next
    return nextCommit.getName
  }

  def findAll(path: String): Seq[String] = {
    import scala.collection.JavaConversions
    
    val walk = createWalkWithFilter(path)
    val seq = JavaConversions.asScalaIterator(walk.iterator).toArray.toSeq
    seq.map { commit => commit.getName } toSeq
	}
}