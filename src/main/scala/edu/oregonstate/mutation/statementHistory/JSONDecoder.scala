package edu.oregonstate.mutation.statementHistory

import play.api.libs.json.Json

object JSONDecoder {

  def decode(json: String) : Seq[MutantInfo] = {
    def decodeLine(line: String): MutantInfo = {
      var parsed = Json.parse(line)
      var file = (parsed \ "mutant" \ "filename").as[String]
      var lineNo = (parsed \ "mutant" \ "line").as[Int]
      var className = (parsed \ "mutant" \ "id" \ "location" \ "class").as[String]
      return new MutantInfo(file, lineNo, className)
    }
    json.split("\n").map(line => {
      decodeLine(line)
    }).toSeq
  }

}
