package edu.oregonstate.mutation.statementHistory

import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by caius on 10/14/15.
 */
class LineInfoTest extends FlatSpec with Matchers {

  it should "find this to be in a line" in {
    new LineInfo(17, 28).contains(20) should be (true)
  }

  it should "find these two to be outside a line" in {
    new LineInfo(17, 28).contains(29) should be (false)
    new LineInfo(17, 28).contains(16) should be (false)
  }

  it should "return the right string" in {
    new LineInfo(17, 28).toString() should equal ("17:28")
  }
}
