#!/bin/bash

WORKING_DIR=`cd $(dirname $0); pwd`

echo "loading environment..."
. ${WORKING_DIR}/environment.sh

NR_NODES_TO_USE=10
EST_DURATION="35"

clean_master
kill_java ${CLUSTER}
clean_slaves ${CLUSTER}

ISPN_PB="-stats -write-skew -versioned -extended-stats -pb-protocol -lock-timeout 500"

w1="-nr-thread 8 -simul-time 2400 -nr-keys 5000 -write-tx-percentage 80 -rd-op-rd-tx 2700:3700 -wrt-op-wrt-tx 9:12 -rd-op-wrt-tx 9:12"

${BENC_GEN} ${w1}  -passive-replication
${ISPN_GEN} ${ISPN_PB}
run_test ${NR_NODES_TO_USE} "test-result" ${EST_DURATION} "PB" ${CLUSTER}


exit 0

