package edu.oregonstate.mutation.statementHistory

import com.brindescu._
import com.brindescu.gumtree.facade.Gumtree._
import com.brindescu.gumtree.facade.SuperTree

object CParser extends ASTParser {
	override def parse(content: String): SuperTree = cdtfacade.CParser.parse(content)
}
