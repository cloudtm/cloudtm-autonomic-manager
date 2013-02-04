#!/bin/bash

## Load includes
if [ "x$RADARGUN_HOME" = "x" ]; then DIRNAME=`dirname $0`; RADARGUN_HOME=`cd $DIRNAME/..; pwd` ; fi; export RADARGUN_HOME
. ${RADARGUN_HOME}/bin/includes.sh
. ${RADARGUN_HOME}/bin/environment.sh

LOG=${RADARGUN_HOME}/beforeBenchmark.out
SLEEP=60
CSV_CP=${RADARGUN_HOME}/lib/WpmCsvReporter.jar:${RADARGUN_HOME}/conf
CSV_CLASS="eu.cloudtm.reporter.CsvReporter"

if [ "$1" == "" ]; then
VM=localhost:9998
else
VM=$1
fi

log() {
echo $1 >> ${LOG}
}

stopMonitor() {
PID=`ps -ef | grep "${CSV_CLASS}" | grep -v "grep" | awk '{print $2}'`
log "killing $PID"
kill -9 $PID
}

startMonitor() {
java -cp ${CSV_CP} ${CSV_CLASS} > ${RADARGUN_HOME}/csv-reporter.out 2>&1 &
log "Monitor started with PID $!"
}


dataplacement() {
log "Send data placement request"
${RADARGUN_HOME}/bin/dataPlacement.sh $VM >> ${LOG} 2>&1
}

block() {
log "Sleeping ${SLEEP} seconds"
sleep ${SLEEP}
}

sblock() {
log "Sleeping ${FIRST_SLEEP} seconds"
sleep ${FIRST_SLEEP}
}

stop() {
log "Stop benchmark"
${RADARGUN_HOME}/bin/stopBenchmark.sh `cat ${RADARGUN_HOME}/slaves` >> ${LOG} 2>&1
}

touch ${LOG}
date > ${LOG}

sblock
stopMonitor
startMonitor

block

for i in {1..10}; do
dataplacement
block
done

stop

exit 0
