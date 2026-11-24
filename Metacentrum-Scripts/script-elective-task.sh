#!/bin/bash
#PBS -l select=1:ncpus=1:mem=4gb:scratch_local=1gb:cluster=zenon
#PBS -e /storage/praha1/home/stahllu/AI_for_games/job_logs
#PBS -o /storage/praha1/home/stahllu/AI_for_games/job_logs

ROOT=/storage/praha1/home/stahllu/AI_for_games
DATADIR=$ROOT/mario
RESULTDIR=$ROOT/results/electiveTask

echo "$PBS_JOBID (TTT=$TTT, DFPMP=$DFPMP) is running on node `hostname -f` in a scratch directory $SCRATCHDIR" >> $DATADIR/jobs_info.txt

module add openjdk-17

test -n "$SCRATCHDIR" || { echo >&2 "Variable SCRATCHDIR is not set"; exit 1; }

cp -R $DATADIR/Mario-AI-Framework $SCRATCHDIR || { echo >&2 "Error while copying input file"; exit 2; }

cd $SCRATCHDIR/Mario-AI-Framework

javac -cp src src/mff/agents/benchmark/ElectiveTaskMetacentrum.java
java -cp src mff.agents.benchmark.ElectiveTaskMetacentrum $TTT $DFPMP

mkdir -p $RESULTDIR
cp -a agent-benchmark/. $RESULTDIR/ || { echo >&2 "Error while copying output file to result dir"; exit 2; }

clean_scratch
