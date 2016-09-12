package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter

import scala.collection.mutable.ListBuffer

object JavaFileFinder {

  def findIn(git: Git, commit: String = "HEAD"): List[String] = {
    val tree = GitUtil.getCommit(git, commit).getTree
    val walk = new TreeWalk(git.getRepository)
    walk.addTree(tree)
    walk.setRecursive(false)
    walk.setFilter(PathSuffixFilter.create(".java"))

    val files = new ListBuffer[String]()
    while (walk.next) {
      if (walk.isSubtree)
        walk.enterSubtree()
      else
        files.append(walk.getPathString)
    }
    files.toList
  }
}
