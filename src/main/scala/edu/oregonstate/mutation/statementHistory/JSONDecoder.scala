package edu.oregonstate.mutation.statementHistory

import play.api.libs.json.Json

object JSONDecoder extends Decoder {

  def decodeLine(line: String): StatementInfo = {
    val parsed = Json.parse(line)
    val file = (parsed \ "mutant" \ "filename").as[String]
    val rawLineNo = parsed \ "mutant" \ "line"
    val lineNo = rawLineNo.asOpt[Int] match {
      case Some(x) => x
      case _ => rawLineNo.as[String].toInt
    }
    val className = (parsed \ "mutant" \ "id" \ "location" \ "class").as[String]
    return new StatementInfo(file, lineNo, className)
  }
}
