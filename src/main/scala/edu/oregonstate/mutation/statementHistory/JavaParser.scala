package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.Gumtree._
import com.brindescu.gumtree.jdt.JavaTree
import com.brindescu.jdtfacade.Parser

object JavaParser extends ASTParser {

  def parse(content: String): JavaTree = {
    Parser.parse(content.toCharArray)
  }
}
