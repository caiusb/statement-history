### Statement Finder ###

This project tracks a statement through history and finds the commits that
touched it. The Main class takes as input a json file and output a series
a CSV like output with the commits.

## Building ##

This project uses sbt. To build, run `sbt package`. To run the tests, run
`sbt testAll`.

To generate eclipse files, run `sbt eclipse`. For IntelliJ Idea, run `sbt 
idea`. You can also simply import the project into Idea, and it should work
fine.

## Licence ##

This code is distributed under the MIT License.