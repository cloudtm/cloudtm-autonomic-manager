#!/bin/bash

CONSUMER_PID=""
PRODUCER_PID=""
WORKING_DIR=`cd $(dirname $0); pwd`
cd $WORKING_DIR;

producer_pid() {
  PRODUCER_PID=`ps -ef | grep "wpm.jar producer" | grep -v grep | awk '{print $2}'`
}

consumer_pid() {
  CONSUMER_PID=`ps -ef | grep "wpm.jar consumer" | grep -v grep | awk '{print $2}'`
}


case $1 in
  -start) OP="start";;
  -stop) OP="stop";;
  *) echo "Unkonwn operation $1"; exit 1;;
esac

echo "WPM controler in "`hostname -s`;
echo "Executing operation: $OP";

producer_pid
consumer_pid

if [ "$OP" == "start" ]; then
  ./log/clean.sh
  if [ -n "$CONSUMER_PID" ]; then
    echo "Consumer already running with PID $CONSUMER_PID. Don't start again!";
  else
WPM_CLASS_PATH=.:wpm.jar:lib/*
EXEC_MAIN=eu.cloudtm.wpm.main.Main
MODULE=consumer

nohup java -cp ${WPM_CLASS_PATH} -Djavax.net.ssl.trustStore=config/serverkeys -Djavax.net.ssl.trustStorePassword=cloudtm -Djavax.net.ssl.keyStore=config/serverkeys -Djavax.net.ssl.keyStorePassword=cloudtm ${EXEC_MAIN} ${MODULE} &> "consumerDebug.log" &


  fi
  if [ -n "$PRODUCER_PID" ]; then
    echo "Producer already running with PID $PRODUCER_PID. Don't start again!";
  else
WPM_CLASS_PATH=.:wpm.jar:lib/*
EXEC_MAIN=eu.cloudtm.wpm.main.Main
MODULE=producer

nohup java -cp ${WPM_CLASS_PATH} ${EXEC_MAIN} ${MODULE} &> "producerDebug.log" &

  fi
else
  echo "Stopping producer (PID=$PRODUCER_PID) and consumer (PID=$CONSUMER_PID)"
  kill -9 $PRODUCER_PID
  kill -9 $CONSUMER_PID
fi

exit 0
