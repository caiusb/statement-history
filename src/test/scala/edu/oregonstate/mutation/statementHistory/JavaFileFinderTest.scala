package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git

/**
 * Created by caius on 11/2/15.
 */
class JavaFileFinderTest extends GitTest {

  it should "find a java file" in {
    add("A.java", "bla")
    val files = JavaFileFinder.findIn(Git.open(repo))
    files should have size 1
    files should equal (List("A.java"))
  }

  it should "find two java files" in {
    add(List("A.java", "B.java"), List("bla","bla"))
    val files = JavaFileFinder.findIn(Git.open(repo))
    files should have size 2
    files should equal (List("A.java", "B.java"))
  }

  it should "find files in subdirs" in {
    add(List("src/A.java", "src/B.java"), List("bla", "bla"))
    val files = JavaFileFinder.findIn(Git.open(repo))
    files should have size 2
    files should equal (List("src/A.java", "src/B.java"))
  }

  it should "only fine java files" in {
    add(List("A.java", "readme.txt"), List("bla", "bla"))
    val files = JavaFileFinder.findIn(Git.open(repo))
    files should have size 1
    files should equal (List("A.java"))
  }
}
