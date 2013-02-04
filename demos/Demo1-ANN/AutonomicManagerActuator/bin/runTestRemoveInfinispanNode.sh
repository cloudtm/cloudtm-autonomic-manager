#!/bin/bash

PWD=`pwd`
EXEC=eu.cloudtm.am.test.TestRemoveInfinispanNode
PATH_SERVER=${PWD}/out/production/AutonomicManagerActuator
LIB=${PWD}/lib/*
CLASS_PATH=${PWD}:${LIB}:${PATH_SERVER}:.
#echo "$CLASS_PATH"

IP_SERVER=$1
PORT_SERVER=$2

if [ $# -eq 2 ]
  then
    java -Djava.net.preferIPv4Stack=true -cp ${CLASS_PATH} ${EXEC} ${IP_SERVER} ${PORT_SERVER}
else
  echo "Usage: ./runTestRemoveInfinispanNode.sh <Actuator Server IP> <Actuator Server Port>"
fi
