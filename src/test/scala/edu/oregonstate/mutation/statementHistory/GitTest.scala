package edu.oregonstate.mutation.statementHistory

import java.io.{File, PrintWriter}
import java.nio.file.Paths

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.dircache.DirCacheEntry
import org.eclipse.jgit.lib.{Constants, PersonIdent}
import org.eclipse.jgit.revwalk.RevCommit
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}


trait GitTest extends FlatSpec with BeforeAndAfterEach with Matchers {
  
  var repo: File = _
  var git: Git = _
  val author = new PersonIdent("Test Person", "test@example.com")
  
  override def beforeEach = {
    repo = initRepo
    repo should not be (null)
    git = Git.open(repo)
  }
  
  override def afterEach {
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
    val file = new File(repo.getAbsolutePath, filename)
    if (!file.getParentFile.exists)
      file.getParentFile.mkdirs
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
    val entry: DirCacheEntry = addFileToIndex(filename, content, git)
    entry should not be (null)
    return commit(message, git)
  }

  private def commit(message: String, git: Git): RevCommit = {
    val result = git.commit.setMessage(message).setAuthor(author).setCommitter(author).call
    result should not be (null)
    return result
  }

  private def addFileToIndex(filename: String, content: String, git: Git): DirCacheEntry = {
    createFile(filename, content)
    val cache = git.add.addFilepattern(filename).call
    val entry = cache.getEntry(filename)
    entry
  }

  def add(fileNames: Seq[String], contents: Seq[String]): RevCommit =
    add(fileNames, contents, "")

  def add(fileNames: Seq[String], content: Seq[String], message: String): RevCommit = {
    val git = Git.open(repo)
    for((file,content) <- fileNames zip content)
      addFileToIndex(file, content, git)
    commit(message, git)
  }
}