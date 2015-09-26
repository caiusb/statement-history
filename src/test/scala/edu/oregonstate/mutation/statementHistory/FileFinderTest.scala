package edu.oregonstate.mutation.statementHistory

class FileFinderTest extends GitTest {
  
  var fileFinder: FileFinder = _
  
  override def beforeEach = {
    super.beforeEach
    fileFinder = new FileFinder(repo.getAbsolutePath)
  }
  
  it should "find the file" in {
    val commit = add("A.java", "public class A{}")
    val sha = fileFinder.findFirst("A.java").getName
    commit.getName should equal (sha)
  }
  
  it should "find the file in more than one commit" in {
     add("B.java", "public class B{]")
     val commit = add("A.java", "public class A{}")
     val sha = fileFinder.findFirst("A.java").getName
     commit.getName should equal (sha)
  }

  it should "find both occurances" in {
    val first = add("A.java", "public class A{}")
    val second = add("A.java", "public class A{public void m(){}}")
    val expectedList = Seq(first getName, second getName)
    val all = fileFinder.findAll("A.java")
    all should have size 2
    all should equal (expectedList)
  }
  
  it should "find both occurances in more than two commits" in {
    val first = add("A.java", "public class A{}")
 		add("B.java", "public class B{}")
    val second = add("A.java", "public class B{}")
    val expectedList = Seq(first getName, second getName)
    val all = fileFinder.findAll("A.java")
    all should have size 2
    all should equal (expectedList)
  }

  it should "find file from incomplete path" in {
    var first = add("src/bla/A.java", "public class A{}")
    val all = fileFinder.findAll("A.java")
    all should have size 1
    all(0) should equal (first getName)
  }
}