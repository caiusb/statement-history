package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git

class StatementFinderTest extends GitTest {

  it should "find statement in simple example" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nint x=3;}}")
    val statement = StatementFinder.findNode(Git.open(repo), commit.getName, "A.java", 3)
    statement should not be null
  }

  it should "find the node in this file" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nint x=3;}}")
    val statements = StatementFinder.findAllNodesForFile(Git.open(repo), commit.getName, "A.java")
    statements should have size 1
  }

  it should "find all the nodes in this file" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nint x=3;\nint y=3;}}")
    val statements = StatementFinder.findAllNodesForFile(Git.open(repo), commit.getName, "A.java")
    statements should have size 2
  }

  it should "not find anything if only a curly brace on the line" in {
    val commit = add("A.java", "public class A{\npublic void m()\n{\nint x=3;\nint y=3;}}")
    val statement = StatementFinder.findNode(Git.open(repo), commit.getName, "A.java", 3)
    statement should be (null)
  }
}