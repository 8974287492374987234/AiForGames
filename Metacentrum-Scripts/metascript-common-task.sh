#!/bin/bash

for ss in 1 2 3 4 5 6 7 8 9 10 20
do
  for ttfw in 0.2 0.4 0.6 0.8 1.0 1.2 1.4 1.6 1.8 2.0
  do
      echo $ss $ttfw
      qsub -v SS=$ss,TTFW=$ttfw script-common-task.sh
  done
done
