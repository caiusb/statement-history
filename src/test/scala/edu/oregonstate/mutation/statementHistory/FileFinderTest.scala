package edu.oregonstate.mutation.statementHistory

import org.scalatest.FlatSpec

class FileFinderTest extends GitTest {
  
  it should "find the file" in {
    val fileFinder = new FileFinder(repo.getAbsolutePath)
    val commit = add("A.java", "public class A{}")
    val sha = fileFinder.findFirst("A.java")
    commit.getName should equal (sha)
  }
}