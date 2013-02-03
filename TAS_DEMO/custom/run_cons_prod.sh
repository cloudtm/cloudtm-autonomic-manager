nohup ./run_consumer.sh > consumer.log &
sleep 1
nohup ./run_producer.sh  > producer.log &