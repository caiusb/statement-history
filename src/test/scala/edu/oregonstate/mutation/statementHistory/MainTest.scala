package edu.oregonstate.mutation.statementHistory

import java.io.File

import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by caius on 10/30/15.
 */
class MainTest extends FlatSpec with Matchers {

  private def parse(opts: String): Main.Config =
    Main.parseCmdOptions(opts.split(" ")).get

  it should "correcly get the commit sha" in {
    parse("-c bla -j c -r x").commit should equal ("bla")
    parse("--commit bla -j c -r x").commit should equal("bla")
  }

  it should "correctly get the json file" in {
    parse("-c c -j test -r r").jsonFile should equal (Some(new File("test")))
    parse("-c c --json-file test -r -r").jsonFile should equal (Some(new File("test")))
  }

  it should "correcly get the repo path" in {
    parse("-c c -j j -r test").repo should equal (new File("test"))
    parse("-c c -j r --repo test").repo should equal(new File("test"))
  }

  it should "correctly get the output file" in {
    parse("-c c -j j -r r --out bla").file should equal(Some("bla"))
    parse("-c c -j j -r r -f bla").file should equal(Some("bla"))

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
}
