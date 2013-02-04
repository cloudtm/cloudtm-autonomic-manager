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

#pragma once
#ifndef _STATES_H
#define _STATES_H


//**********************************

#include "configuration.h"
#include "client/descriptors.h"
#include "lib/events_wait_queue.h"
#include "server/concurrency-control.h"

#include "ROOT-Sim.h"

typedef struct _tx_class_statistics{
	int class_id;
	int tot_tx_num;
	int committed_tx_num;
	int aborted_tx_num;
	double tot_response_time;
}tx_class_statistics;

typedef struct _CPU_state {
	int cpu_id;
	int available_servents;
	int server_id;
	event_wait_queue *requests_queue;
} CPU_state;


typedef struct _SERVER_lp_state_type {
	int server_id;
	//int next_enq_event;
	int stored_values;
	CPU_state cpu_state;
	struct _server_conf_t configuration;
	cc_metadata * cc_metadata;
} SERVER_lp_state_type;

typedef struct _NET_lp_state_type {
	double bandwidth;
	double req_avg_rate;
	double network_congestion;
	double avg_packet_size;
	int served_requests;
	int forwarded_requests;
	int sender_id;
	int receiver_id;
	void *current_pkt_data;
	struct _network_conf_t configuration;
} NET_lp_state_type;

typedef struct _CLIENT_lp_state_type {
	transaction_descriptor *transaction_list;
	tx_class_statistics **tx_classes;
	int client_id;
	int next_transaction_id;
	int aborted_runs;
	int started_runs;
	int committed_transactions;
	int executed_get_operation;
	int executed_put_operation;
	int active_transactions;
	int in_backoff_transactions;
	double client_statements_avg_think_time;
	struct FILE *output_file;
	int ending;
//statistics
	double total_transaction_execution_time;
	double total_transaction_run_execution_time;
	struct _client_conf_t configuration;
} CLIENT_lp_state_type;

typedef struct _state_type {
	int start_stat_time;
	int num_clients;
	int num_servers;
	int cache_objects; // Maximum number of values stored in cache
	int object_replication_degree;
	
	// Depends on what LP type I am
	union {
		CLIENT_lp_state_type	client_state;
		SERVER_lp_state_type	server_state;
	} type;

} state_type;

#endif /* _STATES_H */
