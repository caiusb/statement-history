package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom.{CompilationUnit, ASTNode}

/**
 * Created by caius on 9/25/15.
 */
class StatementInfo(private var fileName: String, private var lineNumber: Int, private var className: String){

  def this(file:String, node: ASTNode) =
    this(file, node.getRoot.asInstanceOf[CompilationUnit].getLineNumber(node.getStartPosition), "")

  def getFileName:String = fileName
  def getLineNumber:Int = lineNumber
  def getClassName:String = className

  def printInfo: String = getFileName + "," + getLineNumber + ","
}
