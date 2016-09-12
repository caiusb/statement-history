package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.{SuperMethod, SuperTree}

object MethodFinder extends NodeFinder {

  override def getMapOfNodes(astRoot: SuperTree): Map[Int, SuperTree] = {
    astRoot.listAllNodes.filter { _.isInstanceOf[SuperMethod] }
      .flatMap { m => m.getSourceRange.map { (_,m) } }.toMap
  }
}
