package edu.oregonstate.mutation.statementHistory

class StatementChangeDetectorTest extends GitTest {
  
  it should "find a statement change in two consecutive commits" in {
    val first = add("A.java", "public class A{\npublic void m(){\nint x=3;\n}\n}")
    val second = add("A.java", "public class A{\npublic void m(){\nint x=2;\n}\n}")
    val expected = Seq(first getName, second getName)
    
    val detector = new StatementChangeDetector(repo getAbsolutePath)
    val commits = detector.findCommits("A.java", 3)
    commits should have size 2
    commits should equal (expected)
  }
}