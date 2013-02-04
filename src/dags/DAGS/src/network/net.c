/*
* CINI, Consorzio Interuniversitario Nazionale per l'Informatica
* Copyright 2013 CINI and/or its affiliates and other
* contributors as indicated by the @author tags. All rights reserved.
* See the copyright.txt in the distribution for a full listing of
* individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 3.0 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

#include <time.h>
#include <stdlib.h>
#include <stdio.h>

#include "../ROOT-Sim.h"

#include "net.h"

void net_print_stat(NET_lp_state_type *pointer, time_type now) {
	printf("%f\t\t%f\t\t%f\t\t%f\t%i\t%i\t\t%f\n", pointer->bandwidth,
	       pointer->req_avg_rate, pointer->network_congestion,
	       pointer->avg_packet_size, pointer->served_requests,
	       pointer->forwarded_requests, now);
	fflush(stdout);
}


void net_send_message(int source,int destination, event_content_type *new_event_content, int num_clients , int num_servers, double now, double average_network_delay) {
	ScheduleNewEvent(destination, now + average_network_delay, DELIVER_MESSAGE,  new_event_content, sizeof(event_content_type));
}

