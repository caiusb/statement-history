package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.lib.Repository
import java.io.File
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.api.Git
import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfter
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.Paths
import org.eclipse.jgit.lib.PersonIdent
import org.scalatest.Matchers
import org.eclipse.jgit.revwalk.RevCommit


trait GitTest extends FlatSpec with BeforeAndAfter with Matchers {
  
  var repo: File = _
  val author = new PersonIdent("Test Person", "test@example.com")
  
  before {
    repo = initRepo
    repo should not be (null)
  }
  
  after {
    repo.delete
  }
  
  def initRepo: File = {
    val tmpDir = System.getProperty("java.io.tmpdir");
    tmpDir should not be (null)
    val repoDir = new File(tmpDir, "git-test-case-" + System.nanoTime + Thread.currentThread.getId)
    repoDir.mkdir should be (true)

    Git.init.setDirectory(repoDir).setBare(false).call
    val dotGitFolder = new File(repoDir, Constants.DOT_GIT)
    dotGitFolder.exists should be (true)
    repoDir.deleteOnExit
    return repoDir
  }
  
  private def createFile(filename: String, content: String) = {
    val path = repo.getAbsolutePath
    val writer = new PrintWriter(Paths.get(path, filename).toAbsolutePath.toString)
    writer should not be null
    writer.write(content)
    writer.close
  }
  
  def add(filename: String, content: String): RevCommit = {
    add(filename, content, "")
  }
  
  def add(filename: String, content: String, message: String): RevCommit = {
    val git = Git.open(repo)
    createFile(filename, content)
    val cache = git.add.addFilepattern(filename).call
    val entry = cache.getEntry(filename)
    entry should not be (null)
    val result = git.commit.setMessage(message).setAuthor(author).setCommitter(author).call
    result should not be (null)
    return result
  }
}