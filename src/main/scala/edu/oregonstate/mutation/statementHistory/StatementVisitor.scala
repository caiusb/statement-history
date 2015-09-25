package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom._

class StatementVisitor() extends ASTVisitor {
  
  val statementMap = scala.collection.mutable.Map[Int, Statement]()
  
  def getStatementMap:Map[Int, Statement] = {
    statementMap.toMap
  }
  
  def visitStatement(node: Statement):Boolean = {
    val startPosition = node.getStartPosition
    val line = node.getRoot.asInstanceOf[CompilationUnit].getLineNumber(startPosition) - 1
    statementMap(line) = node
    return false
  }
  
  override def visit(node: VariableDeclarationStatement):Boolean = {
    visitStatement(node)
  }
  
}