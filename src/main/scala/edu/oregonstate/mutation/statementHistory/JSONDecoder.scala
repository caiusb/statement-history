package edu.oregonstate.mutation.statementHistory

import java.io.File

import play.api.libs.json.Json

import scala.io.Source

object JSONDecoder {

  def decode(file: File) : Seq[MutantInfo] = {
    decode(Source.fromFile(file).mkString)
  }

  def decode(json: String) : Seq[MutantInfo] = {
    def decodeLine(line: String): MutantInfo = {
      var parsed = Json.parse(line)
      var file = (parsed \ "mutant" \ "filename").as[String]
      var rawLineNo = parsed \ "mutant" \ "line"
      var lineNo = rawLineNo.asOpt[Int] match {
        case Some(x) => x
        case _ => rawLineNo.as[String].toInt
      }
      var className = (parsed \ "mutant" \ "id" \ "location" \ "class").as[String]
      return new MutantInfo(file, lineNo, className)
    }
    json.split("\n").map(line => {
      decodeLine(line)
    }).toSeq
  }

}
