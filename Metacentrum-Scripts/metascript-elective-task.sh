#!/bin/bash

for ttt in 5 10 15 20 33 50 75 100 150 200
do
  if [ $ttt -le 50 ]; then
    walltime=2:00:00
  elif [ $ttt -le 100 ]; then
    walltime=4:00:00
  else
    walltime=24:00:00
  fi
  for dfpmp in 0 1 2 3 5 7 10 20 50 100
  do
      echo $ttt $dfpmp $walltime
      qsub -l walltime=$walltime -v TTT=$ttt,DFPMP=$dfpmp script-elective-task.sh
  done
done
