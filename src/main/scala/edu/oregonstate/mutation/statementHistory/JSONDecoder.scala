package edu.oregonstate.mutation.statementHistory

import java.io.File

import org.eclipse.jdt.core.dom.ASTNode
import play.api.libs.json.Json

import scala.io.Source

object JSONDecoder {

  def decode(file: File) : Seq[StatementInfo] = {
    Source.fromFile(file).getLines().map(decodeLine).toSeq
  }

  def decodeLine(line: String): StatementInfo = {
    var parsed = Json.parse(line)
    var file = (parsed \ "mutant" \ "filename").as[String]
    var rawLineNo = parsed \ "mutant" \ "line"
    var lineNo = rawLineNo.asOpt[Int] match {
      case Some(x) => x
      case _ => rawLineNo.as[String].toInt
    }
    var className = (parsed \ "mutant" \ "id" \ "location" \ "class").as[String]
    return new StatementInfo(file, lineNo, className)
  }

  def decode(json: String) : Seq[StatementInfo] = {
    json.split("\n").map(line => {
      decodeLine(line)
    }).toSeq
  }

  def decode(file: File, find: (String, Int) => ASTNode): Seq[StatementInfo] = {
    val statements = Source.fromFile(file).getLines().map(decodeLine).toSeq
    computeExtraInfo(find, statements)
  }

  def decode(json: String, find: (String, Int) => ASTNode) : Seq[StatementInfo] = {
    val statements = decode(json)
    computeExtraInfo(find, statements)
  }

  private def computeExtraInfo(find: (String, Int) => ASTNode, statements: Seq[StatementInfo]): Seq[StatementInfo] = {
    statements.foreach(s => {
      val node = find(s.getFileName, s.getLineNumber)
      s.computeOtherInfo(node)
    })
    return statements
  }
}
