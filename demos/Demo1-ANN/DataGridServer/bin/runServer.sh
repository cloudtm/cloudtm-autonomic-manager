#!/bin/bash

#PORT=$1
#INFINISPAN_CONF=$2


HOST=`hostname`
INFINISPAN_CONF=cloudtm.xml

PWD=`pwd`
EXEC=simutools.infinispan.server.Server
PATH_SERVER=${PWD}/out/production/InfinispanServer
LIB=${PWD}/lib/*
CLASS_PATH=${PWD}:${LIB}:${PATH_SERVER}:.
JVM_OPTS="-server -Xmx1024M -Xms1024M"
JGROUPS_BIND="-Djava.net.preferIPv4Stack=true -Dbind.address=${HOST} -Djgroups.bind_addr=${HOST}"
JMX_BIND="-Dcom.sun.management.jmxremote.port=7992 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
LOG4J="-Dlog4j.configuration=log4j.properties"
#echo "$CLASS_PATH"

java ${JVM_OPTS} ${JGROUPS_BIND} ${JMX_BIND} ${LOG4J} -cp ${CLASS_PATH} ${EXEC} ${HOST} ${INFINISPAN_CONF}
