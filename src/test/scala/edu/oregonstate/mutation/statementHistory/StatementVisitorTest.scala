package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.Statement
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

  private def getStatementMap(stmt: String): Map[Int, Statement] = {
    val cu = putStatementInCU(stmt)
    getAST(cu).accept(visitor)
    val stmtMap = visitor.getStatementMap
    stmtMap
  }

  def checkStatement(stmt: String): Unit =
    assertStatement(getStatementMap(stmt))

  def assertStatement(stmtMap: Map[Int, Statement]): Unit = {
    stmtMap should have size 1
    stmtMap(2) should not be null
  }

  it should "find a variable declaration" in
    checkStatement("int x=3;")

  it should "find a constructor invocation" in
    checkStatement("Object x = new Object();")
}
