#!/bin/bash

PWD=`pwd`
EXEC=simutools.infinispan.test.client.Client
PATH_SERVER=${PWD}/out/production/InfinispanServer
LIB=${PWD}/lib/*
CLASS_PATH=${PWD}:${LIB}:${PATH_SERVER}:.
echo "$CLASS_PATH"

java -cp ${CLASS_PATH} ${EXEC} $1 $2 $3
