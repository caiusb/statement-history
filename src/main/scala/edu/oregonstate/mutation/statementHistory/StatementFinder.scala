package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.{SuperStatement, SuperTree}

object StatementFinder extends NodeFinder {

  override def getMapOfNodes(astRoot: SuperTree): Map[Int, SuperTree] = {
    astRoot.listAllNodes.filter { _.isInstanceOf[SuperStatement] }
        .flatMap{s => s.getSourceRange().map{ (_,s) }}.toMap
  }

}