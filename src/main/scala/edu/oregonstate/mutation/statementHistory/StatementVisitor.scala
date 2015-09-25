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

  override def visit(node: AssertStatement): Boolean =
    visitStatement(node)

  override def visit(node: VariableDeclarationStatement): Boolean =
    visitStatement(node)

  override def visit(node: ConstructorInvocation): Boolean =
    visitStatement(node)

  override def visit(node: ContinueStatement): Boolean =
    visitStatement(node)

  override def visit(node: BreakStatement): Boolean =
    visitStatement(node)

  override def visit(node: DoStatement): Boolean =
    visitStatement(node)

  override def visit(node: EmptyStatement): Boolean =
    visitStatement(node)

  override def visit(node: EnhancedForStatement): Boolean =
    visitStatement(node)

  override def visit(node: ExpressionStatement): Boolean =
    visitStatement(node)

  override def visit(node: ForStatement): Boolean =
    visitStatement(node)

  override def visit(node: IfStatement): Boolean =
    visitStatement(node)

  override def visit(node: LabeledStatement): Boolean =
    visitStatement(node)

  override def visit(node: ReturnStatement): Boolean =
    visitStatement(node)

  override def visit(node: SuperConstructorInvocation): Boolean =
    visitStatement(node)

  override def visit(node: SwitchStatement): Boolean =
    visitStatement(node)

  override def visit(node: WhileStatement): Boolean =
  visitStatement(node)

  //not tested
  override def visit(node: SwitchCase): Boolean =
    visitStatement(node)

  override def visit(node: TryStatement): Boolean =
    visitStatement(node)

  override def visit(node: ThrowStatement): Boolean =
    visitStatement(node)

  override def visit(node: SynchronizedStatement): Boolean =
    visitStatement(node)

  override def visit(node: TypeDeclarationStatement): Boolean =
    visitStatement(node)
}