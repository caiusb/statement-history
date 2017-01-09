package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.{DiffEntry, DiffFormatter, RawTextComparator}
import org.eclipse.jgit.revwalk.{RevCommit, RevTree}
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.gitective.core.{BlobUtils, CommitUtils}

import scala.collection.JavaConverters._

object GitUtil {

  def getDiffs(git: Git, commit: RevCommit): Seq[DiffEntry] = {
    val diff = new DiffFormatter(DisabledOutputStream.INSTANCE)
    diff.setRepository(git.getRepository)
    diff.setDiffComparator(RawTextComparator.DEFAULT)
    diff.setDetectRenames(true)
    var secondTree: RevTree = null
    if (commit.getParentCount != 0)
      secondTree = commit.getParent(0).getTree
    diff.scan(secondTree, commit.getTree).asScala
  }

  def getFileContent(git: Git, commitSHA: String, file: String): String =
    BlobUtils.getContent(git.getRepository, commitSHA, file) match {
      case x: Any => x
      case null => ""
    }

  def getCommit(git: Git, sha: String): RevCommit =
    CommitUtils.getCommit(git.getRepository, sha)

  def findFullPath(git: Git, commitID: String, path: String): String =
    findFullPath(git, getCommit(git, commitID), path)

  def findFullPath(git: Git, commit: RevCommit, path: String): String = {
    val tree = commit.getTree
    val walk = new TreeWalk(git.getRepository)
    walk.addTree(tree)
    walk.setRecursive(true)
    walk.setFilter(PathSuffixFilter.create(path))
    walk.next()
    walk.getPathString
  }
}
