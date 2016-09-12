package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.Gumtree._
import com.brindescu.gumtree.facade.SuperTree
import com.brindescu.gumtree.jdt.JavaTree
import org.eclipse.jdt.core.dom._

class StatementInfo(private var fileName: String, private var lineNumber: Int, private var className: String) {

  private implicit def st(s: SuperTree): ASTNode = s.asInstanceOf[JavaTree]

  private var otherInfo = ""
  private var nodeType = ""
  private var enclosingClass = ""
  private var enclosingMethod = ""

  def this(file: String, node: SuperTree, lineNo: Int) {
    this(file, lineNo, "")
    computeOtherInfo(node)
  }

  def this(file: String, node: SuperTree) = {
    this(file, node.getLineNumber, "")
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
    nodeType = computeNodeType(node)
    enclosingClass = findEnclosingClass(node)
    enclosingMethod = findEnclosingMethod(node)
  }

  def findEnclosingClass(node: ASTNode): String =
    node match {
      case null => ""
      case x : TypeDeclaration => x.getName.getIdentifier
      case x : ASTNode => findEnclosingClass(x.getParent)
    }

  def findEnclosingMethod(node: ASTNode): String =
    node match {
      case null => ""
      case x : MethodDeclaration => x.getName.getIdentifier
      case x : ASTNode => findEnclosingMethod(x.getParent)
    }


  def computeNodeType(node: ASTNode) =
    if (node != null)
      node.getClass.getCanonicalName.split("\\.").last
    else
      ""

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

  def printInfo: String = getFileName + "," + getLineNumber + "," + otherInfo + nodeType + "," + enclosingClass + "," + enclosingMethod + ","
}
