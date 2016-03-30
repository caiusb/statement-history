package edu.oregonstate.mutation.statementHistory

import java.io.File

import scala.io.Source

trait Decoder {

  def decode(file: File) : Seq[StatementInfo] =
    Source.fromFile(file).getLines().map(decodeLine).toSeq

  def decode(json: String) : Seq[StatementInfo] =
    json.split("\n").map(line => {
      decodeLine(line)
    }).toSeq

  def decodeLine(line: String) : StatementInfo
}
