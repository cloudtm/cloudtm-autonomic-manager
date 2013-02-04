#!/bin/bash

LOG=${HOME}/log.out
FIRST_SLEEP=15
SLEEP=60
WPM_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/wpm"
MORPH_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/MorphR"

CURR_DIR=`pwd`

log() {
echo $1 >> ${LOG}
}

workload() {
log "Change workload to $1"
${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/radargun/bin/changeWorkload.sh $1 `cat ${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/radargun/slaves` >> ${LOG} 2>&1
}

block() {
log "Sleeping ${SLEEP} seconds"
sleep ${SLEEP}
}

sblock() {
log "Sleeping ${FIRST_SLEEP} seconds"
sleep ${FIRST_SLEEP}
}

stop() {
log "Stop benchmark"
${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/radargun/bin/stopBenchmark.sh `cat ${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/radargun/slaves` >> ${LOG} 2>&1
}

w1="-nr-keys 5000 -wrt-tx-percent 50 -rd-op-rd-tx 2:2 -wrt-op-wrt-tx 1:1 -rd-op-wrt-tx 1:1"
w2="-nr-keys 1000 -wrt-tx-percent 95 -rd-op-rd-tx 200:200 -wrt-op-wrt-tx 100:100 -rd-op-wrt-tx 100:100"
w3="-nr-keys 1000 -wrt-tx-percent 40 -rd-op-rd-tx 50:50 -wrt-op-wrt-tx 25:25 -rd-op-wrt-tx 25:25"

cd $WPM_DIR
./run_log_service.sh 
cd $CURR_DIR

for node in `cat ${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/radargun/slaves` 
do
ssh $node "cd ${WPM_DIR}; ./buildConfig.sh ${node}"
ssh $node "cd ${WPM_DIR}; ./wpm.sh -start"
done

block

${MORPH_DIR}/buildPropertiesFile.sh  1500000  
${MORPH_DIR}/run.sh  

block

workload "${w1}"

block
block
block
block
block
block

workload "${w2}"

block
block
block
block
block
block

workload "${w3}"

block
block
block
block
block
block


cp ${MORPH_DIR}/proberesults.txt ${MORPH_DIR}/proberesults-Deliverable.txt  >> ${LOG} 2>&1

for node in `cat ${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/radargun/slaves`
do
ssh $node "cd ${WPM_DIR}; ./wpm.sh -stop"
done

exit 0

