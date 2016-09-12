package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.jdt.JavaTree
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.dom.AST._

import com.brindescu.gumtree.facade.Gumtree._

object JavaParser extends ASTParser {
  def parse(content: String): JavaTree = {
    import org.eclipse.jdt.core.dom.ASTParser._
    val parser = newParser(K_COMPILATION_UNIT)
    parser.setKind(JLS8)
    parser.setSource(content.toCharArray)
    parser.setResolveBindings(true)
    parser.createAST(new NullProgressMonitor)
  }
}
