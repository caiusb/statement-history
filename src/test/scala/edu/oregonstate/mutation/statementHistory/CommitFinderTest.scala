package edu.oregonstate.mutation.statementHistory

class CommitFinderTest extends GitTest {
  
  var fileFinder: CommitFinder = _
  
  override def beforeEach = {
    super.beforeEach
    fileFinder = new CommitFinder(repo.getAbsolutePath)
  }
  
  it should "find the file" in {
    val commit = add("A.java", "public class A{}")
    val sha = fileFinder.findFirstCommit("A.java", "HEAD").getName
    commit.getName should equal (sha)
  }
  
  it should "find the file in more than one commit" in {
     add("B.java", "public class B{]")
     val commit = add("A.java", "public class A{}")
     val sha = fileFinder.findFirstCommit("A.java", "HEAD").getName
     commit.getName should equal (sha)
  }

  it should "find both occurances" in {
    val first = add("A.java", "public class A{}")
    val second = add("A.java", "public class A{public void m(){}}")
    val expectedList = Seq(first getName, second getName)
    val all = fileFinder.findAllCommits("A.java", "HEAD")
    all should have size 2
    all should equal (expectedList)
  }
  
  it should "find both occurances in more than two commits" in {
    val first = add("A.java", "public class A{}")
 		add("B.java", "public class B{}")
    val second = add("A.java", "public class B{}")
    val expectedList = Seq(first getName, second getName)
    val all = fileFinder.findAllCommits("A.java", "HEAD")
    all should have size 2
    all should equal (expectedList)
  }

  it should "find file from incomplete path" in {
    var first = add("src/bla/A.java", "public class A{}")
    val all = fileFinder.findAllCommits("A.java", "HEAD")
    all should have size 1
    all(0) should equal (first getName)
  }

  it should "find the file three times" in {
    var first = add("A.java", "public class A{}");
    val second = add("A.java", "public class A2{}");
    var third = add("A.java", "public class A3{}");
    val expected = Seq(first.getName, second.getName, third.getName)
    val all = fileFinder.findAllCommits("A.java", "HEAD")
    all should have size expected.size
    all should equal (expected)
  }

  it should "find part of the history" in {
    var first = add("A.java", "public class A{}")
    val second = add("A.java", "public class A2{}")
    var third = add("A.java", "public class A3{}")
    val expected = List(first.getName, second.getName)
    val all = fileFinder.findAllCommits("A.java", second.getName)
    all should have size expected.size
    all should equal (expected)
  }
}