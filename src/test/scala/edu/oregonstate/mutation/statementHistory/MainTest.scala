package edu.oregonstate.mutation.statementHistory

import java.io.File

import edu.oregonstate.mutation.statementHistory.Main.Config

class MainTest extends GitTest {

  private def parse(opts: String): Main.Config =
    Main.parseCmdOptions(opts.split(" ")).get

  it should "correcly get the commit sha" in {
    parse("-c bla -j c -r x").commit should equal ("bla")
    parse("--commit bla -j c -r x").commit should equal("bla")
  }

  it should "correctly get the json file" in {
    parse("-c c -j test -r r").statementFile should equal (Some(new File("test")))
    parse("-c c --json-file test -r -r").statementFile should equal (Some(new File("test")))
  }

  it should "correcly get the repo path" in {
    parse("-c c -j j -r test").repo should equal (new File("test"))
    parse("-c c -j r --repo test").repo should equal(new File("test"))
  }

  it should "correctly get the output file" in {
    parse("-c c -j j -r r --out bla").file should equal(Some("bla"))
    parse("-c c -j j -r r -f bla").file should equal(Some("bla"))
  }

  it should "correctly get the csv option" in {
    parse("-c c -j j -r r --csv").csvFile should be (true)
  }

  it should "correctly determine the order to be both" in {
    Main.getAnalysisOrder(parse("-c c -j j -r r")) should equal (Order.BOTH)
  }

  it should "correctly determine the order to be forward" in {
    Main.getAnalysisOrder(parse("-c c -j j -r r --forward")) should equal (Order.FORWARD)
  }

  it should "correctly determine the order to be in reverse" in {
    Main.getAnalysisOrder(parse("-c c -j j -r r --reverse")) should equal (Order.REVERSE)
  }

  it should "find all the statements in the repo" in {
    add("A.java", "public class A{public void m(){int x=33;}}")
    add("B.java", "public class B{public void m(){int y=33;}}")
    val finder = StatementFinder
    finder.parser = JavaParser
    Main.getAllNodesInRepo(finder, new Config(repo = repo)) should have size 2
  }

  it should "find all the C statements in the repo" in {
    add("test.c", "void main(){int x=33;}")
    add("test.c", "void main(){int y=33;}")
    val finder = StatementFinder
    finder.parser = CParser
    Main.getAllNodesInRepo(finder, new Config(repo = repo), ".c") should have size 1
  }
}
