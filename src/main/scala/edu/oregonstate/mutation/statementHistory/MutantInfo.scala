package edu.oregonstate.mutation.statementHistory

/**
 * Created by caius on 9/25/15.
 */
class MutantInfo(private var fileName: String, private var lineNumber: Int, private var className: String){

  def getFileName:String = fileName
  def getLineNumber:Int = lineNumber
  def getClassName:String = className
}
