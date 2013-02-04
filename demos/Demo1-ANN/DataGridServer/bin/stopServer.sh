#!/bin/bash

ADDRESS=$1
PWD=`pwd`
EXEC=simutools.infinispan.server.StopServer
PATH_SERVER=${PWD}/out/production/InfinispanServer
LIB=${PWD}/lib/*
CLASS_PATH=${PWD}:${LIB}:${PATH_SERVER}:.
#echo "$CLASS_PATH"

echo "Try to stop Infinispan Server on current node ${ADDRESS}"
java -Djava.net.preferIPv4Stack=true -cp ${CLASS_PATH} ${EXEC} ${ADDRESS}