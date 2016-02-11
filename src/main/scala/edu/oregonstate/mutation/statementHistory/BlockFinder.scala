package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.{CompilationUnit, Block, ASTVisitor, ASTNode}
import scala.collection._

/**
 * Created by caius on 2/11/16.
 */
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

  override def getMapOfNodes(astRoot: ASTNode): immutable.Map[Int, ASTNode] = {
    val visitor = new BlockVisitor
    astRoot.accept(visitor)
    immutable.Map() ++ visitor.blocks
  }
}
