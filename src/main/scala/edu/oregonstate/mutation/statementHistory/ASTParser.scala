package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.SuperTree

trait ASTParser {

	def parse(content: String): SuperTree
}
