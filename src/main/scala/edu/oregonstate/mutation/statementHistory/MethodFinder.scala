package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.{ASTNode, ASTVisitor, CompilationUnit, MethodDeclaration}

object MethodFinder extends NodeFinder {

  private class MethodVisitor extends ASTVisitor {

    val methods = scala.collection.mutable.Map[Int, MethodDeclaration]()

    override def visit(method: MethodDeclaration): Boolean = {
      var root = method.getRoot.asInstanceOf[CompilationUnit]
      val start = root.getLineNumber(method.getStartPosition)
      val end = root.getLineNumber(method.getStartPosition + method.getLength)
      (start to end).foreach(line => methods(line) = method)
      false
    }
  }

  override def getMapOfNodes(astRoot: ASTNode): Map[Int, ASTNode] = {
    val visitor = new MethodVisitor()
    astRoot.accept(visitor)
    Map() ++ visitor.methods
  }
}
