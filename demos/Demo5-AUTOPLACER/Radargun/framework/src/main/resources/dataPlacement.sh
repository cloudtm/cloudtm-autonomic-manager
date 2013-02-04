#!/bin/bash

## Load includes
if [ "x$RADARGUN_HOME" = "x" ]; then DIRNAME=`dirname $0`; RADARGUN_HOME=`cd $DIRNAME/..; pwd` ; fi; export RADARGUN_HOME
. ${RADARGUN_HOME}/bin/includes.sh
. ${RADARGUN_HOME}/bin/environment.sh

CP=${RADARGUN_HOME}/lib/radargun-*.jar
JAVA="org.radargun.DataPlacementJmxRequest"
OBJ="DataPlacementManager"

help_and_exit() {
echo "usage: $0 <slave> [-jmx-mbean <mbean name>]"
echo "   slave: <hostname or hostname:port>"
exit 0;
}

if [ -n "$1" ]; then
slave=$1;
fi

while [ -n "$1" ]; do
case $1 in  
  -jmx-mbean) OBJ=$2; shift 2;;  
  -h) help_and_exit;;
  -*) echo "Unknown option $1"; shift 1;;
  *) SLAVE=$1; shift 1;;
esac
done

if [ -z "$SLAVE" ]; then
echo "Slave not found!";
help_and_exit;
fi

if [[ "$SLAVE" == *:* ]]; then
HOST=`echo $SLAVE | cut -d: -f1`
PORT=`echo $SLAVE | cut -d: -f2`
else
HOST=$SLAVE
PORT=${JMX_SLAVES_PORT}
fi

CMD="java -cp ${CP} ${JAVA} -jmx-component ${OBJ} -hostname ${HOST} -port ${PORT}"
echo $CMD
eval $CMD

exit 0
