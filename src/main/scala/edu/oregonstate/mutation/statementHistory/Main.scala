package edu.oregonstate.mutation.statementHistory

import java.io.File
import java.util.logging.Level

import fr.labri.gumtree.matchers.Matcher

object Main {
  def main(args: Array[String]) = {
    val repo = args(0)
    var jsonFile = args(1)

    disableLoggers()

    val detector = new StatementChangeDetector(repo)
    val mutants = JSONDecoder.decode(new File(jsonFile))

    val result = mutants.toParArray.map(mutant => {
      mutant.getFileName + "," + mutant.getLineNumber + "," +
        detector.findCommits(mutant.getFileName, mutant.getLineNumber).map(commit => commit + ", ")
    })

    print(result)
  }

  def disableLoggers() = {
    Matcher.LOGGER.setLevel(Level.OFF)
  }
}