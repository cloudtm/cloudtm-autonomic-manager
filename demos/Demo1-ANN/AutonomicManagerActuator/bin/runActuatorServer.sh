#!/bin/bash


PWD=`pwd`
EXEC=eu.cloudtm.am.server.ActuatorServer
PATH_SERVER=${PWD}/out/production/AutonomicManagerActuator
LIB=${PWD}/lib/*
CLASS_PATH=${PWD}:${LIB}:${PATH_SERVER}:.
#echo "$CLASS_PATH"

java -Djava.net.preferIPv4Stack=true -cp ${CLASS_PATH} ${EXEC}