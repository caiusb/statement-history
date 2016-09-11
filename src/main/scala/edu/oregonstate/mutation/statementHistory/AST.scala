package edu.oregonstate.mutation.statementHistory

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.dom.AST._
import org.eclipse.jdt.core.dom.ASTNode

object AST {
  def getJavaAST(content: String): ASTNode = {
    import org.eclipse.jdt.core.dom.ASTParser._
    val parser = newParser(K_COMPILATION_UNIT)
    parser.setKind(JLS8)
    parser.setSource(content.toCharArray)
    parser.setResolveBindings(true)
    parser.createAST(new NullProgressMonitor)
  }
}
