package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.{RawTextComparator, DiffFormatter, DiffEntry}
import org.eclipse.jgit.revwalk.{RevTree, RevCommit}
import org.eclipse.jgit.util.io.DisabledOutputStream
import org.gitective.core.BlobUtils

import scala.collection.JavaConversions

/**
 * Created by caius on 9/25/15.
 */
object GitUtil {

  def getDiffs(git: Git, commit: RevCommit): Seq[DiffEntry] = {
    val diff = new DiffFormatter(DisabledOutputStream.INSTANCE)
    diff.setRepository(git.getRepository)
    diff.setDiffComparator(RawTextComparator.DEFAULT)
    diff.setDetectRenames(true)
    var secondTree: RevTree = null
    if (commit.getParentCount != 0)
      secondTree = commit.getParent(0).getTree
    val diffs = diff.scan(secondTree, commit.getTree)
    JavaConversions.asScalaBuffer(diffs)
  }

  def getFileContent(git: Git, commitSHA: String, file: String): String = {
    BlobUtils.getContent(git.getRepository, commitSHA, file) match {
      case x: Any => x
      case null => ""
    }
  }
}
