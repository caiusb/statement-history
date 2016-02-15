package edu.oregonstate.mutation.statementHistory

import org.eclipse.jdt.core.dom._

/**
 * Created by caius on 9/25/15.
 */
class StatementInfo(private var fileName: String, private var lineNumber: Int, private var className: String) {

  private var otherInfo = ""

  def this(file: String, node: ASTNode, lineNo: Int) {
    this(file, lineNo, "")
    computeOtherInfo(node)
  }

  def this(file: String, node: ASTNode) = {
    this(file, node.getRoot.asInstanceOf[CompilationUnit].getLineNumber(node.getStartPosition), "")
    computeOtherInfo(node)
  }

  def computeOtherInfo(node: ASTNode) = {
    otherInfo = node match {
      case b: Block =>
        getBlockInfo(b)
      case m: MethodDeclaration =>
        getMethodInfo(m)
      case _ => ""
    }
  }

  private def getBlockInfo(b: Block): String = {
    val root = b.getRoot.asInstanceOf[CompilationUnit]
    val t = b.getParent match {
      case _: IfStatement => "if("
      case _: ForStatement => "for("
      case _: EnhancedForStatement => "for("
      case _: WhileStatement => "while("
      case _: DoStatement => "do("
      case _: MethodDeclaration => "method("
      case _: TryStatement => "try("
      case _: CatchClause => "catch("
      case _: Initializer => "static("
      case _ => "other("
    }
    t + root.getLineNumber(b.getStartPosition) + ":" + root.getLineNumber(b.getStartPosition + b.getLength) + "),"
  }

  private def getMethodInfo(m: MethodDeclaration): String = m.getName + ","

  def getFileName: String = fileName

  def getLineNumber: Int = lineNumber

  def getClassName: String = className

  def printInfo: String = getFileName + "," + getLineNumber + "," + otherInfo
}
