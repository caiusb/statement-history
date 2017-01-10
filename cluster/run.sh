#!/bin/bash

export PATH="$HOME/.local/bin:$HOME/jdk8/bin:$PATH"

echo "HERE"

repo=$1

tmpFolder="/tmp/$JOB_ID"
repoFolder="$tmpFolder/$repo"
jsonFile="$tmpFolder/$repo.json"

mkdir -p $tmpFolder
pushd $tmpFolder
rsync -az babylon02.eecs.oregonstate.edu:/scratch/brindesc/icse17-corpus/$repo .
rsync -az babylon02.eecs.oregonstate.edu:/scratch/brindesc/merged-lines/$repo.json .
popd

echo $JOB_ID
echo $repoFolder
echo $jsonFile

java -Xmx4G -jar "../target/scala-2.12/statement-history-assembly-0.9.1.jar" -j $jsonFile -r $repoFolder > $HOME/merging/tracking/$repo.txt

rm -rf $tmpFolder
