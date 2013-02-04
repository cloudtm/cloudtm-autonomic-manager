#!/bin/bash

WORKING_DIR=`cd $(dirname $0); pwd`
AUTOPLACER=${WORKING_DIR}/../../src/autoplacer/
DIST=${WORKING_DIR}/dist

cd ${AUTOPLACER};
mvn clean
cd -

cd ${WORKING_DIR}/Radargun
mvn clean
cd -

cd ${WORKING_DIR}/Csv-reporter
ant clean
cd -


rm -r ${DIST}/* 2>/dev/null

echo "done!"

exit 0;
