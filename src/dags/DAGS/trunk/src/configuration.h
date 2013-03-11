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
#ifndef _CONFIGURATION_H
#define _CONFIGURATION_H

#include "ROOT-Sim.h"

#define NUM_OBJECTS 1024

#ifndef __ROOT_Sim_H
typedef enum {false, true}bool;
typedef double time_type;
#endif

enum _cc_type_t {
	ETL_2PL, CTL_2PL, PRIMARY_OWNER_CTL_2PL, GMU
};

enum _system_model_t {
	OPEN, CLOSED
};

enum _workload_type_t {
	SYNTHETIC, TPCC
};

enum _distribution {
	FIXED, UNIFORM, ZIPF, SINGLE_BAR, TOPKEYS
};

struct _client_conf_t {
	enum _system_model_t system_model;

	double tx_arrival_rate; // this is used with OPEN system type only
	enum _workload_type_t workload_type;

	double inter_transaction_think_time;
	double inter_tx_operation_think_time;
	double backoff_time;

	int number_of_transactions;
	int number_of_threads;

	// transactions configuration starts here
	int number_of_tx_classes;
	enum _distribution transaction_length_type;
	int *tx_class_length; // transaction's length expressed as number of operations
	int *object_access_distribution_type;
	double *tx_class_probability; // probability to produce a transaction of a given class
	float *topkeys_cache_objects_get_probability; // probability to get an object with a specific key
	int number_of_gets_topkeys;
	float *topkeys_cache_objects_put_probability; // probability to put an object with a specific key
	int number_of_puts_topkeys;
	double *tx_class_write_probability;

	enum _distribution data_items_access_distribution;
	double data_items_zipf_const;

	int number_of_blocks;
	double *blocks_access_probability;

	bool tlm_print_stat;
	bool client_verbose;
	bool tlm_verbose;
	bool client_print_execution_info;
	bool client_print_stat;

};

struct _server_conf_t {
	int replication_degree;
	int max_servents_per_cpu;
	enum _cc_type_t concurrency_control_type;
	double locking_timeout;

	// CPU service demand configuration starts here
	double local_tx_put_cpu_service_demand;
	double local_tx_get_cpu_service_demand;
	double remote_tx_put_cpu_service_demand;
	double remote_tx_get_cpu_service_demand;
	double tx_begin_cpu_service_demand;
	double tx_abort_cpu_service_demand;
	double local_tx_get_from_remote_cpu_service_demand;
	double tx_prepare_cpu_service_demand;
	double tx_send_remote_tx_get_cpu_service_demand;
	double remote_tx_get_return_cpu_service_demand;
	double update_cpu_service_demand;
	double local_prepare_successed_cpu_service_demand;
	double local_prepare_failed_cpu_service_demand;
	double distributed_final_tx_commit_cpu_service_demand;
	double local_tx_final_commit_cpu_service_demand;
	double tx_prepare_failed_cpu_service_demand;
	int cpu_queue_length;
	bool deadlock_detection_enabled;

	bool cc_print_stat;
	bool print_max_blocked_transactions;

	bool server_verbose;
	bool tlm_verbose;
	bool cc_verbose;
};

struct _network_conf_t {

	double net_bandwidth;
	double avg_packet_size;
	double network_delay;
	double time_for_send_net_req;
	double time_for_net_deliver;

	bool net_verbose;
};

struct _simulation_conf {

	int start_stat_time;
	int num_clients;
	int num_servers;
	int cache_objects;
	int object_replication_degree;
	double average_server_to_server_net_delay;
	double average_client_to_server_net_delay;
	struct _client_conf_t client_conf;
	struct _server_conf_t server_conf;
	struct _network_conf_t network_conf;
};

typedef struct _simulation_conf simulation_conf;

#endif /* _CONFIGURATION_H */
