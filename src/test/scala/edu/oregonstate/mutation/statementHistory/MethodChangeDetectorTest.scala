package edu.oregonstate.mutation.statementHistory

/**
 * Created by caius on 12/5/15.
 */
class MethodChangeDetectorTest extends GitTest with NodeChangeDetectorTest {

  it should "track a method across refactorings" in {
    val first = add("A.java", "public class A{\npublic void m(){\nint x=3;\n}}")
    val second = add("A.java", "public class A{\npublic void n(){\nint x=3;\n}}")
    val expected = Seq(ci(first.getName, "ADD"), ci(second.getName, "UPDATE"))

    val actual = nd(git, MethodFinder).findCommits("A.java", 3, second.getName)

    actual should equal (expected)
  }

  it should "track a method across multiple refactorings" in {
    val first = add("A.java", "public class A{\npublic void m(){\nint x=3;\n}}")
    val second = add("A.java", "public class A{\npublic void n(int a){\nint x=3;\n}}")

    val expected = Seq(ci(first.getName, "ADD"), ci(second.getName, "UPDATE"))

    val actual = nd(git, MethodFinder).findCommits("A.java", 3, second.getName)

    actual should equal (expected)
  }

}
