package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom._

class StatementVisitor(lineInfo: Seq[LineInfo]) extends ASTVisitor {
  
  val statementMap = scala.collection.mutable.Map[Int, Statement]()
  
  def getStatementMap:Map[Int, Statement] = {
    statementMap.toMap
  }
  
  def visitStatement(node: Statement):Boolean = {
    val startPosition = node.getStartPosition
    val statementLines = lineInfo.indices.filter { i => lineInfo(i).contains(startPosition) }
    statementMap(statementLines(0)) = node
    return false
  }
  
  override def visit(node: VariableDeclarationStatement):Boolean = {
    visitStatement(node)
  }
  
}