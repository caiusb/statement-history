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

  it should "find a statement in three consecutive commits" in {
    val first = add("A.java", "public class A{\npublic void m(){\nint x=3;\n}\n}")
    val second = add("A.java", "public class A{\npublic void m(){\nint x=2;\n}\n}")
    val third = add("A.java", "public class A{\npublic void m(){\nint x=10;\n}\n}")
    val expected = Seq(first getName, second getName, third getName)
    val detector = new StatementChangeDetector(repo getAbsolutePath)
    val commits = detector.findCommits("A.java", 3)
    commits should have size 3
    commits should equal (expected)
  }

  it should "find a statement if the line number changes" in {
    val first = add("A.java", "public class A{\npublic void m(){\nint x=3;\n}\n}")
    val second = add("A.java", "public class A{\npublic void m(){\n//some comment\nint x=2;\n}\n}")
    val third = add("A.java", "public class A{\npublic void m(){\n//some comment\nint x=10;\n}\n}")
    val expected = Seq(first getName, second getName, third getName)

    val detector = new StatementChangeDetector(repo getAbsolutePath)
    val commits = detector.findCommits("A.java", 3)
    commits should have size 3
    commits should equal (expected)
  }

  it should "find a statement if another one was added" in {
    val first = add("A.java", "public class A{\npublic void m(){\nint x=3;\n}\n}")
    val second = add("A.java", "public class A{\npublic void m(){\nint y=33;\nint x=2;\n}\n}")
    val expected = Seq(first getName, second getName)

    val commits = new StatementChangeDetector(repo getAbsolutePath).findCommits("A.java", 3)
    commits should have size 2
    commits should equal (expected)
  }

  it should "know if something was deleted" in {
    val first = add("A.java", "public class A{\npublic void m(){\nint x=3;\n}\n}")
    val second = add("A.java", "public class A{\npublic void m(){}\n}")
    add("A.java", "public class A{\npublic void m(){}\npublic void n(){}}")
    val expected = Seq(first getName, second getName)

    val commits = new StatementChangeDetector(repo getAbsolutePath).findCommits("A.java", 3)
    commits should have size 2
    commits should equal (expected)
  }

  it should "find the commit with a partial path" in {
    val first = add("src/A.java", "public class A{\npublic void m(){\nint x=3;\n}\n}")
    val second = add("src/A.java", "public class A{\npublic void m(){}\n}")
    val expected = Seq(first getName, second getName)

    val commits = new StatementChangeDetector(repo getAbsolutePath).findCommits("A.java", 3)
    commits should have size 2
    commits should equal (expected)
  }
}