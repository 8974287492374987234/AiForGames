#!/bin/bash

for ttt in 5 10 15 20 33 50 75 100 150 200
do
  walltime=$((400 * ttt + 1800))
  walltime=$(printf "%d:%02d:00" $((walltime / 3600)) $((walltime % 3600 / 60)))
  for dfpmp in 0 1 2 3 5 7 10 20 50 100
  do
      echo $ttt $dfpmp $walltime
      qsub -l walltime=$walltime -v TTT=$ttt,DFPMP=$dfpmp script-elective-task.sh
  done
done
