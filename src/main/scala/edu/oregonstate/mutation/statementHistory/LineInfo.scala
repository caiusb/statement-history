package edu.oregonstate.mutation.statementHistory

class LineInfo(startPos: Int, endPos: Int) {
    val start = startPos
    val end = endPos
    
    def contains(pos: Int): Boolean = {
      pos >= start && pos <= end
    }
    
    override def toString():String = {
      start + ":" + end
    }
 }