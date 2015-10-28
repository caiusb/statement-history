package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.{ASTNode, ASTVisitor, CompilationUnit, MethodDeclaration}

/**
 * Created by caius on 10/12/15.
 */
object MethodFinder extends NodeFinder {

  private class MethodVisitor(private val line: Integer) extends ASTVisitor {

    var method: MethodDeclaration = _

    override def visit(method: MethodDeclaration): Boolean = {
      var root = method.getRoot.asInstanceOf[CompilationUnit]
      val start = root.getLineNumber(method.getStartPosition)
      val end = root.getLineNumber(method.getStartPosition + method.getLength)
      if (start <= line && line <= end )
        this.method=method

      false
    }
  }
  override def findNode(line: Int, root: ASTNode): ASTNode = {
    val visitor = new MethodVisitor(line)
    root.accept(visitor)
    visitor.method
  }
}
