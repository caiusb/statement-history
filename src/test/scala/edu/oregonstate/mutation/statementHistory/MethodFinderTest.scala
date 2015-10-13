package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.MethodDeclaration
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by caius on 10/12/15.
 */
class MethodFinderTest extends FlatSpec with Matchers {

  it should "find the method" in {
    val content = "public class A{\npublic void m(){}}"
    val root = AST.getAST(content)
    val method = MethodFinder.findNode(2, root)
    method shouldBe a [MethodDeclaration]
    method.asInstanceOf[MethodDeclaration].getName.getIdentifier should equal ("m")
  }

}
