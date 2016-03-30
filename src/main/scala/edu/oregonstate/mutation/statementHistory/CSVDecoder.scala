package edu.oregonstate.mutation.statementHistory

import org.apache.commons.csv.{CSVFormat, CSVParser}

object CSVDecoder extends Decoder {

  override def decodeLine(line: String): StatementInfo = {
    val parser = CSVParser.parse(line, CSVFormat.DEFAULT)
    val record = parser.getRecords.get(0)
    val sha = record.get(0)
    val lineNo = record.get(1).toInt
    val file = record.get(2).toString
    new StatementInfo(file, lineNo, "")
  }
}
