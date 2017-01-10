#!/bin/bash

projects=$1

while read p
do
    qsub -l a=lx-amd64 -l mem_free=4G -cwd -S /bin/bash -o $HOME/merging/tracking/log/$p.txt -e $HOME/merging/tracking/log/$p.txt ./run.sh $p
done < $projects
