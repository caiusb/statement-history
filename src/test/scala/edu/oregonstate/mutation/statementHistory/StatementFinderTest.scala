package edu.oregonstate.mutation.statementHistory

class StatementFinderTest extends GitTest {
  
  it should "find statement in simple example" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nint x=3;}}")
    val finder = new StatementFinder(repo.getAbsolutePath)
    val statement = finder.findStatement(commit.getName, "A.java", 3)
    statement should not be (null)
  }
  
}