package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.{Statement, ASTNode}

object StatementFinder extends NodeFinder {

  override def getMapOfNodes(astRoot: ASTNode): Map[Int, Statement] = {
    val visitor = new StatementVisitor()
    astRoot.accept(visitor)
    visitor.getStatementMap
  }
}