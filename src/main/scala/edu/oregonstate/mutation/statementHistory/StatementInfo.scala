package edu.oregonstate.mutation.statementHistory

/**
 * Created by caius on 9/25/15.
 */
class StatementInfo(private var fileName: String, private var lineNumber: Int, private var className: String){

  def getFileName:String = fileName
  def getLineNumber:Int = lineNumber
  def getClassName:String = className

  def printInfo: String = getFileName + "," + getLineNumber + ","
}
