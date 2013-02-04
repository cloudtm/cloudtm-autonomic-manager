#!/bin/bash

bash ./log/clean.sh
echo "clean done!"

WPM_CLASS_PATH=.:wpm.jar:lib/*
EXEC_MAIN=eu.cloudtm.wpm.main.Main
MODULE=consumer

java -cp ${WPM_CLASS_PATH} -Djavax.net.ssl.trustStore=config/serverkeys -Djavax.net.ssl.trustStorePassword=cloudtm -Djavax.net.ssl.keyStore=config/serverkeys -Djavax.net.ssl.keyStorePassword=cloudtm ${EXEC_MAIN} ${MODULE}
