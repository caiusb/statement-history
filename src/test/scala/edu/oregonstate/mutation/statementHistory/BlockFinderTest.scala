package edu.oregonstate.mutation.statementHistory

import org.eclipse.jgit.api.Git

class BlockFinderTest extends GitTest {

  it should "find the block" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nint x=3;\n}}")
    val node = BlockFinder.findNode(Git.open(repo), commit.getName, "A.java", 3)
    node should not be null
  }

  it should "find the innermost block" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nif(true)\n{int x=3;}}}")
    val node = BlockFinder.findNode(Git.open(repo), commit.getName, "A.java", 4)
    node.getLineNumber should equal (4)
  }
}