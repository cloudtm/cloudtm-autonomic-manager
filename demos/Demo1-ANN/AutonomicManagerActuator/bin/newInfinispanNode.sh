#!/bin/bash

IP=$1
REMOTE_DIR=$2
SSH_USER=$3
echo "I want to run Infinispan node on ${IP}"

if [ $# -eq 3 ]
  then
    echo "Try ssh ${SSH_USER}@${IP} cd ${REMOTE_DIR}; ./bin/runServer.sh&>outputServer&"
    ssh ${SSH_USER}@${IP} "cd ${REMOTE_DIR}; ./bin/runServer.sh&>outputServer&"
    echo "Waiting for Infinispan Server on ${IP}..."
    RET=$(ssh ${SSH_USER}@${IP} "cd ${REMOTE_DIR}; grep accepting outputServer")

    while [ ${RET} -eq  ]
    do
    RET=$(ssh ${SSH_USER}@${IP} "cd ${REMOTE_DIR}; grep accepting outputServer")

    wait 1
    done
    echo "Infinispan Server running on ${IP}"

fi