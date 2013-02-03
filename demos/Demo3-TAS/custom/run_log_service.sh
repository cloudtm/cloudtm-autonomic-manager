#!/bin/bash

bash ./log/clean.sh
echo "clean done!"

WPM_CLASS_PATH=.:wpm.jar:lib/*
EXEC_MAIN=eu.cloudtm.wpm.main.Main
MODULE=logService

java -cp ${WPM_CLASS_PATH} -Djavax.net.ssl.keyStore=config/serverkeys -Djavax.net.ssl.keyStorePassword=cloudtm -Djavax.net.ssl.trustStore=config/serverkeys -Djavax.net.ssl.trustStorePassword=cloudtm -Djava.net.preferIPv4Stack=true -Dbind.address=127.0.0.1 ${EXEC_MAIN} ${MODULE} > logService.log &
