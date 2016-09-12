package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.{SuperBlock, SuperTree}
import org.eclipse.jdt.core.dom.{ASTVisitor, Block, CompilationUnit}

import scala.collection._

object BlockFinder extends NodeFinder {

  private class BlockVisitor extends ASTVisitor {
    val blocks = mutable.Map[Int, Block]()

    override def visit(node: Block): Boolean = {
      val root = node.getRoot.asInstanceOf[CompilationUnit]
      val startLine = root.getLineNumber(node.getStartPosition)
      val endLine = root.getLineNumber(node.getStartPosition + node.getLength)
      (startLine to endLine).foreach(l => blocks(l) = node)
      true
    }
  }

  override def getMapOfNodes(astRoot: SuperTree): immutable.Map[Int, SuperTree] = {
    astRoot.listAllNodes.filter { _.isInstanceOf[SuperBlock] }
      .flatMap{b => b.getSourceRange().map{ (_,b) }}.toMap
  }
}
