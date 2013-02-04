#!/bin/bash

WPM_CLASS_PATH=.:wpm.jar:lib/*
EXEC_MAIN=eu.cloudtm.wpm.main.Main
MODULE=producer

java -cp ${WPM_CLASS_PATH} ${EXEC_MAIN} ${MODULE}




