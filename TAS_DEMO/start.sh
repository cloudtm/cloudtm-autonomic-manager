set -e
RG_HOME=RadargunTASDemo
WPM_HOME=wpm
CONTROLLER_HOME=ControllerTas

if [ -z "$1" ]; then
   echo "Error: missing argument"
   echo "Usage: ./start \"list_of_ip_for_the_benchmark\""
   echo "Exiting"
   exit 1
fi

echo "Going to run the TAS demo"
echo "This node will host the master process of the Radargun Framework, the wpm logService and the Controller responsible for retrieving statistics and performin what-if analysis"
echo "The TPC-C benchmark will be executed on the following machines $1"

echo "Starting the Radargun master and the LogService"
cd ${RG_HOME}
nohup ./bin/benchmark.sh $1 > bench.log &
sleep 1
echo "Starting the TasController"
cd ..
cd ${CONTROLLER_HOME}
touch tas.log
nohup bash run.sh &> tas.log &
cd ..
echo "The demo is up and running. Plese wait for the system to reach a stable state."
echo " Periodically, the TAS controller will produce plots with its performance forecast"
tail -f ${CONTROLLER_HOME}/tas.log

exit 0
