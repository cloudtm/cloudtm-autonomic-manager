#!/bin/bash

## Load includes
if [ "x$RADARGUN_HOME" = "x" ]; then DIRNAME=`dirname $0`; RADARGUN_HOME=`cd $DIRNAME/..; pwd` ; fi; export RADARGUN_HOME
. ${RADARGUN_HOME}/bin/includes.sh
. ${RADARGUN_HOME}/bin/environment.sh

CP=${RADARGUN_HOME}/lib/radargun-*.jar
JAVA="org.radargun.WorkloadJmxRequest"
OBJ="-jmx-component BenchmarkStage"

help_and_exit() {
echo "usage: $0 [-jmx-component <mbean>] [-wrt-op-wrt-tx <value>] [-rd-op-wrt-tx <value>] [-rd-op-rd-tx <value>] [-wrt-tx-percent <value>] [-nr-keys <value>] [-stop]"
exit 0;
}

while [ -n "$1" ]; do
case $1 in
  -jmx-component) OBJ="-jmx-component $2"; shift 2;;
  -wrt-op-wrt-tx) WRT="-wrt-op-wrt-tx $2"; shift 2;;
  -rd-op-wrt-tx) RD_WRT="-rd-op-wrt-tx $2"; shift 2;;
  -rd-op-rd-tx) RD_RD="-rd-op-rd-tx $2"; shift 2;;
  -wrt-tx-percent) WRT_TX="-write-percentage $2"; shift 2;;
  -nr-keys) NR_KEYS="-nr-keys $2"; shift 2;;
  -stop) STOP="-stop"; shift 1;;
  -h) help_and_exit;;
  -*) echo "Unknown option $1"; shift 1;;
  *) SLAVES=${SLAVES}" "$1; shift 1;;
esac
done

if [ -z "$SLAVES" ]; then
echo "No slaves found!";
help_and_exit;
fi

for slave in ${SLAVES}; do

if [[ "$slave" == *:* ]]; then
HOST="-hostname "`echo $slave | cut -d: -f1`
PORT="-port "`echo $slave | cut -d: -f2`
else
HOST="-hostname "$slave
PORT="-port "${JMX_SLAVES_PORT}
fi

CMD="java -cp ${CP} ${JAVA} ${OBJ} ${WRT} ${RD_WRT} ${RD_RD} ${WRT_TX} ${NR_KEYS} ${STOP} ${HOST} ${PORT}"
echo $CMD
eval $CMD

done
exit 0