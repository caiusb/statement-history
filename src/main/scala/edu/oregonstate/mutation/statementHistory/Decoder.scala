package edu.oregonstate.mutation.statementHistory

import java.io.File

import com.brindescu.gumtree.facade.Gumtree._
import com.brindescu.gumtree.facade.SuperTree
import com.brindescu.gumtree.jdt.JavaTree
import org.eclipse.jdt.core.dom.ASTNode

import scala.io.Source

trait Decoder {

  private implicit def st(s: SuperTree): ASTNode = s.asInstanceOf[JavaTree]

  def decode(file: File) : Seq[StatementInfo] =
    Source.fromFile(file).getLines().map(decodeLine).toSeq

  def decode(json: String) : Seq[StatementInfo] =
    json.split("\n").map(line => {
      decodeLine(line)
    }).toSeq

  def decode(file: File, find: (String, Int) => SuperTree): Seq[StatementInfo] = {
    val statements = Source.fromFile(file).getLines().map(decodeLine).toSeq
    computeExtraInfo(find, statements)
  }

  def decode(json: String, find: (String, Int) => SuperTree) : Seq[StatementInfo] = {
    val statements = decode(json)
    computeExtraInfo(find, statements)
  }

  private def computeExtraInfo(find: (String, Int) => SuperTree, statements: Seq[StatementInfo]): Seq[StatementInfo] = {
    statements.foreach(s => {
      val node = find(s.getFileName, s.getLineNumber)
      s.computeOtherInfo(node)
    })
    return statements
  }

  def decodeLine(line: String) : StatementInfo
}
