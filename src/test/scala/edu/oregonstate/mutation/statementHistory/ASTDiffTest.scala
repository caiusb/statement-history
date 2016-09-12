package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.JavaASTDiff
import com.github.gumtreediff.actions.model.Update
import org.scalatest.{FlatSpec, Matchers}

class ASTDiffTest extends FlatSpec with Matchers {

  it should "correctly find the diff from String" in {
    val actions = JavaASTDiff.getDiff("public class A{}", "public class B{}").getActions
    actions should have size 1
    actions(0) shouldBe an [Update]
  }
}
