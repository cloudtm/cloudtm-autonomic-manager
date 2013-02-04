#!/bin/sh
###### This script is designed to be called from other scripts, to set environment variables including the bind
###### for cache products, as well as any JVM options.

### Set your bind address for the tests to use. Could be an IP, host name or a reference to an environment variable.
BIND_ADDRESS=${MYTESTIP_2}
JG_FLAGS="-Dresolve.dns=false -Djgroups.timer.num_threads=4"
JVM_OPTS="-server"
JVM_OPTS="$JVM_OPTS -Xmx8G -Xms8G"
#If using the cpu service time per thread, then use this optimization
JVM_OPTS="$JVM_OPTS -XX:+UseLinuxPosixThreadCPUClocks"
#allocate more memory if needed
#JVM_OPTS="$JVM_OPTS -Xmx8G -Xms8G"
#JVM_OPTS="$JVM_OPTS -Xmx16G -Xms16G"
#choose on of the GC types (or none if you want to use the default)
JVM_OPTS="$JVM_OPTS -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
#JVM_OPTS="$JVM_OPTS -XX:+UseParallelGC -XX:+UseParallelOldGC"
JVM_OPTS="$JVM_OPTS $JG_FLAGS"
JPROFILER_HOME=${HOME}/jprofiler6
JPROFILER_CFG_ID=103
JMX_MASTER_PORT="9999"
JMX_SLAVES_PORT="9996"

