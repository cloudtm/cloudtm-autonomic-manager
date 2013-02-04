#!/bin/bash

IP=$1
REMOTE_DIR=$2
SSH_USER=$3
echo "I want to remove Infinispan Server on ${IP}"

if [ $# -eq 3 ]
  then
    echo "Try ssh ${SSH_USER}@${IP} cd ${REMOTE_DIR}; ./bin/stopServer.sh ${IP}"
    ssh ${SSH_USER}@${IP} "cd ${REMOTE_DIR}; ./bin/stopServer.sh ${IP}"
    echo "Removed Infinispan Server on ${IP}"
fi

