package edu.oregonstate.mutation.statementHistory

import java.io.{StringReader, File}

import org.scalatest.{Matchers, FlatSpec}

import scala.io.Source

/**
 * Created by caius on 9/25/15.
 */
class JSONDecoderTest extends FlatSpec with Matchers {

  it should "decode one line" in {
    var resource = getClass.getResource("/oneline.json")
    var content = Source.fromFile(new File(resource.getFile)).mkString
    val result = JSONDecoder.decode(content)
    result should not be null
    result should have size 1
    result(0).getClassName should equal ("net.awired.aclm.argument.CliDefaultParser")
    result(0).getFileName should equal ("CliDefaultParser.java")
    result(0).getLineNumber should equal (314)
  }

  it should "decode two lines" in {
    var resource = getClass.getResource("/twolines.json")
    var content = Source.fromFile(new File(resource.getFile)).mkString
    val result = JSONDecoder.decode(content)
    result should not be null
    result should have size 2
    result(1).getClassName should equal("net.awired.aclm.argument.CliDefaultParser")
    result(1).getFileName should equal ("CliDefaultParser.java")
    result(1).getLineNumber should equal (88)
  }

}
