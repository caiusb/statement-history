package edu.oregonstate.mutation.statementHistory

import com.brindescu.gumtree.facade.{Named, SuperBlock, SuperMethod, SuperTree}

class StatementInfo(private var fileName: String,
                    private var lineNumber: Int,
                    private var className: String,
                    private var sha: String = "HEAD") {

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

  def computeOtherInfo(node: SuperTree) = {
    otherInfo = node match {
      case b: SuperBlock =>
        getBlockInfo(b)
      case m: SuperMethod =>
        getMethodInfo(m)
      case _ => ""
    }
    nodeType = computeNodeType(node)
    enclosingClass = findEnclosingClass(node)
    enclosingMethod = findEnclosingMethod(node)
  }

  def findEnclosingClass(node: SuperTree): String = node.getEnclosingClass match {
    case Some(x) => x.asInstanceOf[Named].getIdentifier
    case None => ""
  }

  def findEnclosingMethod(node: SuperTree): String = node.getEnclosingMethod match {
      case Some(x) => x.asInstanceOf[Named].getIdentifier()
      case None => ""
    }

  def computeNodeType(node: SuperTree) = node.getNodeType()

  private def getBlockInfo(b: SuperBlock): String = b.getBlockInfo

  private def getMethodInfo(m: SuperMethod): String = m.getIdentifier() + ","

  def getFileName: String = fileName

  def getLineNumber: Int = lineNumber

  def getClassName: String = className

  def printInfo: String = getFileName + "," + getLineNumber + "," + otherInfo  + nodeType + "," + enclosingClass + "," + enclosingMethod + ","
}
