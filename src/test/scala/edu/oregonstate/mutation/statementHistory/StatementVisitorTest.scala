package edu.oregonstate.mutation.statementHistory

import edu.oregonstate.mutation.statementHistory.AST._
import org.eclipse.jdt.core.dom.{ASTNode, Statement, VariableDeclarationStatement}
import org.scalatest._

class StatementVisitorTest extends FlatSpec with Matchers with BeforeAndAfter {

  private var visitor: StatementVisitor = _

  before {
    visitor = new StatementVisitor
  }

  private def putStatementInCU(statement: String): String = {
    "public class A {\npublic void m(){\n" + statement + "}}"
  }

  private def getStatementMap(stmt: String): Map[Int, Statement] = {
    val cu = putStatementInCU(stmt)
    val ast = getAST(cu)
    ast.accept(visitor)
    visitor.getStatementMap
  }

  private def checkCU(stmt: String, expected: Int): ASTNode = {
    val visitor = new StatementVisitor
    getAST(stmt).accept(visitor)
    assertStatement(visitor.getStatementMap, expected)
  }

  private def checkCU(stmt: String): ASTNode =
    checkCU(stmt, 1)

  def checkStatement(stmt: String): ASTNode =
    checkStatement(stmt, 1)

  def checkStatement(stmt: String, expected: Int): ASTNode =
    assertStatement(getStatementMap(stmt), expected)

  def assertStatement(stmtMap: Map[Int, Statement]): ASTNode =
    assertStatement(stmtMap, 1)

  def assertStatement(stmtMap: Map[Int, Statement], expected: Int): ASTNode = {
    stmtMap should have size expected
    stmtMap(3) should not be null
    stmtMap(3)
  }

  it should "find a variable declaration" in
    checkStatement("int x=3;")

  //it should "find an assert statement" in
  //  checkStatement("assert true;")

  it should "find a constructor invocation" in
    checkStatement("this();")

  it should "find a continue statement" in
    checkStatement("continue;")

  it should "find a break statement" in
    checkStatement("break;")

  it should "find a do statement" in
    checkStatement("do {\nint x=3;}\nwhile(true);", 3)

  it should "not find a empty statement" in {
    val map = getStatementMap(";")
    map should have size 0
  }

  it should "find an enhanced for statement" in
    checkStatement("for(Object x : new Array[2]){}")

  it should "find a for statement" in
    checkStatement("for(int i=0; i<10; i++){}")

  it should "find an if statement" in
    checkStatement("if(1 != 2){}")

  it should "find a labeled statement" in
    checkStatement("label: ;")

  it should "find a return statement" in
    checkCU("public class A {\npublic int m(){\nreturn 2;}}")

  it should "find a super constructor invocation" in
    checkCU("public class A {\npublic A(){\nsuper();}}")

  it should "find a switch statement" in
    checkStatement("switch(3){\ncase 3: ;}", 2)

  it should "find a switch case statement" in
    checkCU("public class A {\npublic void m() { switch(3){\ncase 3: ;}}}", 2)

  it should "find a while statement" in
    checkStatement("while(true){;}")

  it should "find a statement in a try block" in {
    val node = checkCU("public class A{\npublic void m() { try{\nint x=3;\n}catch(Exception e){}}}", 3)
    node shouldBe an [VariableDeclarationStatement]
  }

  it should "find a statement spanning multiple lines" in {
    val stmtMap = getStatementMap("int \nx=3;")
    stmtMap should have size 2
    stmtMap(4) should not be null
    stmtMap(4) shouldBe a [VariableDeclarationStatement]
  }
}
