package edu.oregonstate.mutation.statementHistory

import org.scalatest.FlatSpec

class FileFinderTest extends GitTest {
  
  var fileFinder: FileFinder = _
  
  override def beforeEach = {
    super.beforeEach
    fileFinder = new FileFinder(repo.getAbsolutePath)
  }
  
  it should "find the file" in {
    val commit = add("A.java", "public class A{}")
    val sha = fileFinder.findFirst("A.java")
    commit.getName should equal (sha)
  }
  
  it should "find the file in more than one commit" in {
     add("B.java", "public class B{]")
     val commit = add("A.java", "public class A{}")
     val sha = fileFinder.findFirst("A.java")
     commit.getName should equal (sha)
  }

}