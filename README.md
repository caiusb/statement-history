[![Stories in Ready](https://badge.waffle.io/caiusb/statement-history.png?label=ready&title=Ready)](https://waffle.io/caiusb/statement-history)
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

I use [Travis CI](http://travis-ci.org) for continous integration: [![Build Status](https://travis-ci.org/caiusb/statement-history.svg)](https://travis-ci.org/caiusb/statement-history)

I use [Coveralls](http://coveralls.io) for test coverage: [![Coverage Status](https://coveralls.io/repos/caiusb/statement-history/badge.svg?branch=master&service=github)](https://coveralls.io/github/caiusb/statement-history?branch=master)

## License ##

Copyright 2015 Caius Brindescu

This code is distributed under the MIT License.