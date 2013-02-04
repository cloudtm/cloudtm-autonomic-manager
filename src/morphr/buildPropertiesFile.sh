#!/bin/bash

MORPHR_DIR="${HOME}/MorphR"

namingHosts="10.100.0.30 10.100.0.31 10.100.0.32 10.100.0.33 10.100.0.34 10.100.0.35 10.100.0.36 10.100.0.37 10.100.0.38 10.100.0.39"
namingPort=8081
runDuration=$1
toQuery="true"
outputFileName="proberesults.txt"
modelFilename="protocol"
modelUsesTrees="true"
forceStop="true"
abort="true"

echo " " > ${MORPHR_DIR}/jmx.properties

echo "namingHosts=${namingHosts}
namingPort=${namingPort}
runDuration=${runDuration}
toQuery=${toQuery}
outputFileName=${outputFileName} " > ${MORPHR_DIR}/jmx.properties

echo " " > ${MORPHR_DIR}/oracle.properties

echo "modelFilename=${modelFilename}
modelUsesTrees=${modelUsesTrees}
forceStop=${forceStop} 
abort=${abort}" > ${MORPHR_DIR}/oracle.properties
