package edu.oregonstate.mutation.statementHistory

import org.scalatest._
import AST._

class StatementVisitorTest extends FlatSpec with Matchers with BeforeAndAfter {

  private var visitor:StatementVisitor = _

  before {
    visitor = new StatementVisitor
  }

  private def putStatementInCU(statement: String): String = {
    "public class A {\npublic void m(){\n" + statement + "}}"
  }

  it should "find a variable declaration" in {
    val stmt = putStatementInCU("int x=3;")
    getAST(stmt).accept(visitor)
    val stmtMap = visitor.getStatementMap
    stmtMap should have size 1
    stmtMap(2) should not be null
  }
}
