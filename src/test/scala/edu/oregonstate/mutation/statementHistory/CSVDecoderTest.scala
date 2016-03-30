package edu.oregonstate.mutation.statementHistory

import java.io.File

import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by caius on 3/30/16.
 */
class CSVDecoderTest extends FlatSpec with Matchers {

  it should "parse a simple CSV" in {
    val info = CSVDecoder.decode(new File(getClass.getResource("/oneline.csv").getFile))
    info should have size 1
  }

}
