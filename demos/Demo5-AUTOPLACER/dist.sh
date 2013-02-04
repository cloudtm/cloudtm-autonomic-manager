#!/bin/bash

WORKING_DIR=`cd $(dirname $0); pwd`
AUTOPLACER=${WORKING_DIR}/../../src/autoplacer/
DIST=${WORKING_DIR}/dist

cd ${AUTOPLACER};
mvn clean install -DskipTests
cd -

cd ${WORKING_DIR}/Radargun
mvn clean install -DskipTests;
cd -

cd ${WORKING_DIR}/Csv-reporter
ant dist
cd -

mkdir -p ${DIST} 2>/dev/null

cp -r ${WORKING_DIR}/Radargun/target/distribution/RadarGun-1.1.1-SNAPSHOT/* ${DIST}/
cp ${WORKING_DIR}/Csv-reporter/dist/lib/WpmCsvReporter.jar  ${DIST}/lib/
cp ${WORKING_DIR}/Csv-reporter/config.properties ${DIST}/conf

mkdir -p ${DIST}/ml
cp ${WORKING_DIR}/Machine-learner/* ${DIST}/ml/

echo "done!"

exit 0;

