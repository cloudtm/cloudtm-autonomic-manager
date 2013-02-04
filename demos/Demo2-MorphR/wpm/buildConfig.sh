#!/bin/bash

#PRODUCER:
hostname=$1

echo "
#PRODUCER:
Consumer_DP_IP_Address=127.0.0.1
Consumer_DP_port_number=7000
Consumer_IP_IP_Address=127.0.0.1
Consumer_IP_local_port_number=7150
Consumer_IP_remote_port_number=7100
hostName=${hostname}
Producer_IP_Address=
#Producer_IP_Address=10.100.0.58
Producer_Group=G01
Producer_Provider=P01
CPU_Component_ID=C_${hostname}
MEM_Component_ID=M_${hostname}
NET_Component_ID=Net_${hostname}
DSK_Component_ID=D_${hostname}
JMX_Component_ID=J_${hostname}
Collect_Timeout=70000" > config/resource_controller.config


