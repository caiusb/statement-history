package edu.oregonstate.mutation.statementHistory

import java.io.File

import org.eclipse.jgit.api.Git
import play.api.libs.json.Json

import scala.io.Source

object JSONDecoder {

  def decode(file: File) : Seq[StatementInfo] = {
    decode(Source.fromFile(file).mkString)
  }

  def decode(json: String) : Seq[StatementInfo] = {
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
    json.split("\n").map(line => {
      decodeLine(line)
    }).toSeq
  }

  def decode(file: File, git: Git, commit: String, finder: NodeFinder): Seq[StatementInfo] =
    decode(Source.fromFile(file).mkString, git, commit, finder)

  def decode(json: String, git: Git, commit: String, finder: NodeFinder) : Seq[StatementInfo] = {
    val statements = decode(json)
    statements.foreach(s => {
      val node = finder.findNode(git, commit, s.getFileName, s.getLineNumber)
      s.computeOtherInfo(node)
    })
    return statements
  }
}
