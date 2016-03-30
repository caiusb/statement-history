package edu.oregonstate.mutation.statementHistory

import java.io.File

import org.eclipse.jgit.api.Git
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

/**
 * Created by caius on 9/25/15.
 */
class JSONDecoderTest extends GitTest {

  it should "decode one line" in {
    val resource = getClass.getResource("/oneline.json")
    val content = Source.fromFile(new File(resource.getFile)).mkString
    val result = JSONDecoder.decode(content)
    result should not be null
    result should have size 1
    result(0).getClassName should equal ("net.awired.aclm.argument.CliDefaultParser")
    result(0).getFileName should equal ("CliDefaultParser.java")
    result(0).getLineNumber should equal (314)
  }

  it should "decode two lines" in {
    val resource = getClass.getResource("/twolines.json")
    val content = Source.fromFile(new File(resource.getFile)).mkString
    val result = JSONDecoder.decode(content)
    result should not be null
    result should have size 2
    result(1).getClassName should equal("net.awired.aclm.argument.CliDefaultParser")
    result(1).getFileName should equal ("CliDefaultParser.java")
    result(1).getLineNumber should equal (88)
  }

  it should "decode this tricky line" in {
    val resource = getClass.getResource("/trickyline.json")
    val result = JSONDecoder.decode(new File(resource.getFile))
    result should have size 1
  }

  it should "decode with a repo in a file" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nint x=3;}}")
    val resource = getClass.getResource("/repo.json")
    val statements = JSONDecoder.decode(new File(resource.getFile), (x: String, n: Int) => BlockFinder.findNode(Git.open(repo), commit.getName, x, n))
    statements should have size 1
    statements(0).printInfo should equal ("A.java,3,method(2:3),Block,")
  }

  it should "decode with a repo in a string" in {
    val commit = add("A.java", "public class A{\npublic void m(){\nint x=3;}}")
    val resource = getClass.getResource("/repo.json")
    val statements = JSONDecoder.decode(Source.fromFile(new File(resource.getFile)).mkString, (x: String, n: Int) => BlockFinder.findNode(Git.open(repo), commit.getName, x, n))
    statements should have size 1
    statements(0).printInfo should equal ("A.java,3,method(2:3),Block,")
  }
}
