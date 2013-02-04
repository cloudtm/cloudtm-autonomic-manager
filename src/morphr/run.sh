#/bin/bash

MORPHR_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/MorphR"
WPM_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/wpm"
WPMConnector_DIR="${HOME}/cloudtm-autonomic-manager/demos/Demo2-MorphR/Workload_Monitor_Connector"

dir=`pwd`

cd ${MORPHR_DIR}

cp="switchmanager.jar:.:${WPM_DIR}/lib/WPMConnector.jar:${WPM_DIR}/wpm.jar:${WPM_DIR}/lib/LatticeCloudTM.jar:${WPM_DIR}/lib/*:${WPMConnector_DIR}/WPMConnector.jar"
javaOpts=""

LD_LIBRARY_PATH=`pwd`
export LD_LIBRARY_PATH

java ${javaOpts} -classpath ${cp} switchmanager.SwitchManager 2>err.log 1>out.log & 

cd ${dir}
