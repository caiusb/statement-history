package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.ASTNode

object StatementFinder extends NodeFinder {

  override def findNode(lineNumber: Int, ast: ASTNode): ASTNode = {
    val visitor = new StatementVisitor()
    ast.accept(visitor)
    visitor.getStatementMap.get(lineNumber - 1) match {
      case Some(x) => return x
      case None => return null
    }
  }
}