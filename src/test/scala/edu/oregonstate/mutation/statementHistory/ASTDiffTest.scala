package edu.oregonstate.mutation.statementHistory

import fr.labri.gumtree.actions.model.Update
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by caius on 10/14/15.
 */
class ASTDiffTest extends FlatSpec with Matchers {

  it should "correctly find the diff from String" in {
    val (actions, _) = ASTDiff.getActions("public class A{}", "public class B{}")
    actions should have size 1
    actions(0) shouldBe an [Update]
  }

  it should "correcty find the diff from Trees" in {
    val (actions, _) = ASTDiff.getActions(ASTDiff.getTree("public class A{}"), ASTDiff.getTree("public class B{}"))
    actions should have size 1
    actions(0) shouldBe an [Update]
  }

}
