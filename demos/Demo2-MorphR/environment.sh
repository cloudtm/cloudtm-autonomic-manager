# ENVIRONMENT VARIABLES
MASTER=`hostname -s`
GSD_CLUSTER=`echo node{20..29}`
CLOUDTM_CLUSTER=`echo vm{47..56}`

#variables to set:
CLUSTER=$GSD_CLUSTER
RADARGUN_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/radargun"
SWITCHMANAGER_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/switchmanager"
RESULTS_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/results-radargun/radargunDeliverable"
WPM_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/wpm"

#uncoment to start gossip router (used in futuregrid)
#GOSSIP_ROUTER=1

LOGS_DIR=${RADARGUN_DIR}/logs
KEYS_DIR=${RADARGUN_DIR}/keys

ISPN_GEN=${RADARGUN_DIR}/plugins/infinispan4/bin/config-generator.sh
JGRP_GEN=${RADARGUN_DIR}/plugins/infinispan4/bin/jgrp-generator.sh
BENC_GEN=${RADARGUN_DIR}/bin/config-generator.sh

export BENCH_XML_FILEPATH=${RADARGUN_DIR}/conf/benchmark.xml
export ISPN_CONFIG_FILENAME=ispn.xml
export ISPN_CONFIG_FILEPATH=${RADARGUN_DIR}/plugins/infinispan4/conf/${ISPN_CONFIG_FILENAME}

RC="READ_COMMITTED"
RR="REPEATABLE_READ"

mkdir -p ${RESULTS_DIR}
mkdir -p ${LOGS_DIR}
mkdir -p ${KEYS_DIR}

copy_to_all() {
for node in $@; do
echo "copy to ${node}"
if [ "${MASTER}" == "${node}" ]; then
echo "not copying... is the master!"
else
ssh ${node} mkdir -p ${RADARGUN_DIR} 
scp -r ${RADARGUN_DIR}/* ${node}:${RADARGUN_DIR} > /dev/null
ssh ${node} mkdir -p ${WPM_DIR}
scp -r ${WPM_DIR}/* ${node}:${WPM_DIR} > /dev/null
fi
done
}

kill_java() {
for node in $@; do
echo "killing java process from ${node}"
ssh ${node} "killall -9 java"
done
}

save_logs() {
nr_nodes=${1}
shift
mkdir -p ${LOGS_DIR}/${nr_nodes}nodes
for node in $@; do
echo "save logs from ${node}"
scp ${node}:${RADARGUN_DIR}/*.out ${LOGS_DIR}/${nr_nodes}nodes/ > /dev/null
done
cp ${RADARGUN_DIR}/*.out ${LOGS_DIR}/${nr_nodes}nodes/ > /dev/null
cp ${RADARGUN_DIR}/*.log ${LOGS_DIR}/${nr_nodes}nodes/ > /dev/null
}

save_keys() {
nr_nodes=${1}
shift
mkdir -p ${KEYS_DIR}/${nr_nodes}nodes
for node in $@; do
echo "save key from ${node}"
scp "${node}:${RADARGUN_DIR}/keys-*" ${KEYS_DIR}/${nr_nodes}-nodes/ > /dev/null
done
for node in $@; do
ssh ${node} "rm ${RADARGUN_DIR}/keys-*"
done
}

clean_slaves() {
for node in $@; do
echo "cleaning slave ${node}"
if [ "${MASTER}" == "${node}" ]; then
echo "not cleaning... is the master!"
else
ssh ${node} "rm -r ${RADARGUN_DIR} ; rm -r ${WPM_DIR}/*" > /dev/null
#ssh ${node} "rm -r ${RADARGUN_DIR}/* ; rm -r ${WPM_DIR}/*" > /dev/null
#ssh ${node} "rm -r ${WPM_DIR}/*" > /dev/null
fi
done
}

clean_master() {
rm -r ${RADARGUN_DIR}/reports/*
rm ${RADARGUN_DIR}/*
rm -r ${LOGS_DIR}/*
rm -r ${KEYS_DIR}/*
}

start_gossip_router() {
echo "start gossip router in ${MASTER}"
java -cp ${RADARGUN_DIR}/plugins/infinispan4/lib/jgroups*.jar org.jgroups.stack.GossipRouter > /dev/null &
}

wait_until_test_finish() {
local MASTER_PID="";
#60 minutes max waiting time (+ estimated test duration)
for ((j = 0; j < 120; ++j)); do
MASTER_PID=`ps -ef | grep "org.radargun.LaunchMaster" | grep "mcouceiro" | grep -v "grep" | awk '{print $2}'`
echo "Checking if the master finished..."
if [ -z "${MASTER_PID}" ]; then
echo "Master finished! No PID found! returning... @" `date`
return;
fi
echo "Master is running. PID is ${MASTER_PID}. @" `date`
sleep 30s
done
echo "Timeout!! Master is running. PID is ${MASTER_PID}. @" `date`
}


run_test() {
echo "======================================================================="
echo "Radargun test"

nr_nodes=${1};
shift
file_prefix=${1};
shift
estimated_duration=${1}
shift
protocolInUse=${1}
shift
nodes=$@;

echo "run with nodes: ${nr_nodes}"
echo "folder suffix: ${file_prefix}"
echo "estimated duration: ${estimated_duration} minutes"
echo "run in nodes: ${nodes}"

echo "======================================================================="
#return
echo "copy to all nodes"
copy_to_all ${nodes}

echo "starting tests with this number of nodes: ${nr_nodes}"
for i in $(echo "${nr_nodes}" | tr "," "\n"); do

if [ -n "${GOSSIP_ROUTER}" ]; then
	start_gossip_router
fi

echo "start test with ${i} number of nodes"
cd ${RADARGUN_DIR}
./bin/benchmark.sh -m 10.100.0.1 -i ${i} ${nodes}
echo "started at" $(date);


echo "wait ${estimated_duration} minutes";
sleep ${estimated_duration}m;

wait_until_test_finish

echo "kill all java";
./bin/master.sh -stop
kill_java ${nodes}
save_logs ${i} ${nodes}
save_keys ${i} ${nodes}
done;

echo "all tests finishes... getting data..."
results="${RESULTS_DIR}/test-result-${file_prefix}"
mkdir -p ${results}
cd ${results}
echo "Copying reports to ${results}"
cp ${RADARGUN_DIR}/reports/*.csv . > /dev/null
echo "Copying Logs"
mkdir logs
cp -r ${LOGS_DIR}/* logs/ > /dev/null
echo "Copying Keys"
mkdir keys
cp -r ${KEYS_DIR}/* keys/ > /dev/null
echo "Copying configuration files"
mkdir config
cp ${RADARGUN_DIR}/conf/benchmark.xml config/ > /dev/null
cp ${RADARGUN_DIR}/plugins/infinispan4/conf/ispn.xml config/ > /dev/null
cp ${RADARGUN_DIR}/plugins/infinispan4/conf/jgroups/jgroups.xml config/ > /dev/null

echo "all complete! cleaning nodes..."
clean_master
clean_slaves ${nodes}
}

