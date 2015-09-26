package edu.oregonstate.mutation.statementHistory

import java.io.File

object Main {
  def main(args: Array[String]) {
    val repo = args(0)
    var jsonFile = args(1)

    val detector = new StatementChangeDetector(repo)
    val mutants = JSONDecoder.decode(new File(jsonFile))

    val result = mutants.map(mutant => {
      mutant.getFileName + "," + mutant.getLineNumber + "," +
        detector.findCommits(mutant.getFileName, mutant.getLineNumber).map(commit => commit + ", ")
    })

    print(result)
  }
}