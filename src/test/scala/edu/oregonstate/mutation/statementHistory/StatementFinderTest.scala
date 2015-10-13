package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git

class StatementFinderTest extends GitTest {
  
  it should "find statement in simple example" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nint x=3;}}")
    val statement = StatementFinder.findNode(Git.open(repo), commit.getName, "A.java", 3)
    statement should not be null
  }
}