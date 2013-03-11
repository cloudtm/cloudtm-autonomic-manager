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

#include <stdio.h>
#include <ctype.h>
#include <string.h>
#include <stddef.h>

#include "configuration.h"

// How much we can read from a line and from a field
#define MAX_LINE 10500 //TODO: [Pietro] Modificato (prima era 150) per permettere la lettura degli array 'Top Keys'
#define MAX_LEN	50

void print_configuration(simulation_conf *configuration) {

	printf("\n\n Current Configuration:\n\n");

	// General Configuration
	printf("General:\n");
	printf("\t num_clients: %d\n", configuration->num_clients);
	printf("\t num_servers: %d\n", configuration->num_servers);
	printf("\t cache_objects: %d\n", configuration->cache_objects);
	printf("\t object_replication_degree: %d\n", configuration->object_replication_degree);
	printf("\t average_server_to_server_net_delay: %f\n", configuration->average_server_to_server_net_delay);
	printf("\t average_client_to_server_net_delay: %f\n", configuration->average_client_to_server_net_delay);

	// Client Configuration
	printf("\nClients:\n");
	printf("\t system_model: %d\n", configuration->client_conf.system_model);
	printf("\t tx_arrival_rate: %f\n", configuration->client_conf.tx_arrival_rate);
	printf("\t workload_type: %d\n", configuration->client_conf.workload_type); //TODO: Alessandro: tipizzare
	printf("\t inter_transaction_think_time: %f\n", configuration->client_conf.inter_transaction_think_time);
	printf("\t inter_tx_operation_think_time: %f\n", configuration->client_conf.inter_tx_operation_think_time);
	printf("\t backoff_time: %f\n", configuration->client_conf.backoff_time);
	printf("\t number_of_transactions: %d\n", configuration->client_conf.number_of_transactions);
	printf("\t number_of_tx_classes: %d\n", configuration->client_conf.number_of_tx_classes);
	printf("\t transaction_length_type: %d\n", configuration->client_conf.transaction_length_type);
	// TODO: for su tx_class_length (int), tx_class_probability (double), tx_class_write_probabilty (double)
	printf("\t data_items_access_distribution: %d\n", configuration->client_conf.data_items_access_distribution);
	printf("\t data_items_zipf_const: %f\n", configuration->client_conf.data_items_zipf_const);
	printf("\t number_of_blocks: %d\n", configuration->client_conf.number_of_blocks);

	// Server Configuration
	printf("\nServers:\n");
	printf("\t replication_degree: %d\n", configuration->server_conf.replication_degree);
	printf("\t max_servents_per_cpu: %d\n", configuration->server_conf.max_servents_per_cpu);
	printf("\t concurrency_control_type: %d\n", configuration->server_conf.concurrency_control_type);
	printf("\t locking_timeout: %f\n", configuration->server_conf.locking_timeout);
	printf("\t local_tx_put_cpu_service_demand: %f\n", configuration->server_conf.local_tx_put_cpu_service_demand);
	printf("\t local_tx_get_cpu_service_demand: %f\n", configuration->server_conf.local_tx_get_cpu_service_demand);
	printf("\t remote_tx_put_cpu_service_demand: %f\n", configuration->server_conf.remote_tx_put_cpu_service_demand);
	printf("\t remote_tx_get_cpu_service_demand: %f\n", configuration->server_conf.remote_tx_get_cpu_service_demand);
	printf("\t tx_begin_cpu_service_demand: %f\n", configuration->server_conf.tx_begin_cpu_service_demand);
	printf("\t tx_abort_cpu_service_demand: %f\n", configuration->server_conf.tx_abort_cpu_service_demand);
	printf("\t local_tx_get_from_remote_cpu_service_demand: %f\n", configuration->server_conf.local_tx_get_from_remote_cpu_service_demand);
	printf("\t remote_tx_get_return_cpu_service_demand: %f\n", configuration->server_conf.remote_tx_get_return_cpu_service_demand);
	printf("\t update_cpu_service_demand: %f\n", configuration->server_conf.update_cpu_service_demand);
	printf("\t local_prepare_successed_cpu_service_demand: %f\n", configuration->server_conf.local_prepare_successed_cpu_service_demand);
	printf("\t local_prepare_failed_cpu_service_demand: %f\n", configuration->server_conf.local_prepare_failed_cpu_service_demand);
	printf("\t distributed_final_tx_commit_cpu_service_demand: %f\n", configuration->server_conf.distributed_final_tx_commit_cpu_service_demand);
	printf("\t local_tx_final_commit_cpu_service_demand: %f\n", configuration->server_conf.local_tx_final_commit_cpu_service_demand);
	printf("\t tx_prepare_failed_cpu_service_demand: %f\n", configuration->server_conf.tx_prepare_failed_cpu_service_demand);
	printf("\t cpu_queue_length: %d\n", configuration->server_conf.cpu_queue_length);
	printf("\t deadlock_detection_enabled: %d\n", configuration->server_conf.deadlock_detection_enabled);
}

/****************************************************/
/******** Table to drive parsing of the conf ********/
/****************************************************/

// List of supported values for parameters
enum param_t {
	NO_TYPE, INT_T, DOUBLE_T, BOOL_T, ARRAY_INT_T, ARRAY_DOUBLE_T, CC_TYPE_T, SYSTEM_MODEL_T, WORKLOAD_TYPE_T, DISTRIBUTION_T
};

// Structure for defining a table to drive the parsing
struct parse_table {
	char *name;
	enum param_t type;
	size_t displacement; // This tells the parser where to store the parsed value!
};

// These are the actual tables to drive parsing, one for each section of the conf file
struct parse_table global_params[] = { { "num_clients", INT_T, offsetof(simulation_conf, num_clients) }, { "num_servers", INT_T, offsetof(simulation_conf, num_servers) }, { "cache_objects", INT_T,
		offsetof(simulation_conf, cache_objects) }, { "object_replication_degree", INT_T, offsetof(simulation_conf, object_replication_degree) }, { "start_stat_time", INT_T, offsetof(simulation_conf,
		start_stat_time) }, { "average_server_to_server_net_delay", DOUBLE_T, offsetof(simulation_conf, average_server_to_server_net_delay) }, { "average_client_to_server_net_delay", DOUBLE_T,
		offsetof(simulation_conf, average_client_to_server_net_delay) }, { NULL, NO_TYPE, 0 } };

struct parse_table client_params[] = { {"system_model", SYSTEM_MODEL_T, offsetof(struct _client_conf_t, system_model)}, {"tx_arrival_rate", DOUBLE_T,
		offsetof(struct _client_conf_t, tx_arrival_rate)},
	{	"workload_type", WORKLOAD_TYPE_T, offsetof(struct _client_conf_t, workload_type)}, // TODO: Alessandro: fixme!
	{	"inter_transaction_think_time", DOUBLE_T, offsetof(struct _client_conf_t, inter_transaction_think_time)}, {"inter_tx_operation_think_time", DOUBLE_T,
		offsetof(struct _client_conf_t, inter_tx_operation_think_time)}, {"backoff_time", DOUBLE_T, offsetof(struct _client_conf_t, backoff_time)}, {"number_of_transactions", INT_T,
		offsetof(struct _client_conf_t, number_of_transactions)}, {"number_of_threads", INT_T, offsetof(struct _client_conf_t, number_of_threads)}, {"number_of_tx_classes", INT_T,
		offsetof(struct _client_conf_t, number_of_tx_classes)}, {"transaction_length_type", DISTRIBUTION_T, offsetof(struct _client_conf_t, transaction_length_type)}, {"tx_class_length",
		ARRAY_INT_T, offsetof(struct _client_conf_t, tx_class_length)}, {"object_access_distribution_type", ARRAY_INT_T, offsetof(struct _client_conf_t, object_access_distribution_type)}, {
		"tx_class_probability", ARRAY_DOUBLE_T, offsetof(struct _client_conf_t, tx_class_probability)}, {"tx_class_write_probability", ARRAY_DOUBLE_T,
		offsetof(struct _client_conf_t, tx_class_write_probability)}, {"data_items_access_distribution", DISTRIBUTION_T, offsetof(struct _client_conf_t, data_items_access_distribution)},
		{"cache_objects_get_probability", ARRAY_DOUBLE_T, offsetof(struct _client_conf_t, topkeys_cache_objects_get_probability)},{"cache_objects_put_probability",ARRAY_DOUBLE_T,
		offsetof(struct _client_conf_t, topkeys_cache_objects_put_probability)},{"data_items_zipf_const", DOUBLE_T, offsetof(struct _client_conf_t, data_items_zipf_const)}, {"number_of_blocks", INT_T,
				offsetof(struct _client_conf_t, number_of_blocks)}, {"blocks_access_probability", ARRAY_DOUBLE_T, offsetof(struct _client_conf_t, blocks_access_probability)},
	{	"tlm_print_stat", BOOL_T, offsetof(struct _client_conf_t, tlm_print_stat)}, {"tlm_verbose", BOOL_T, offsetof(struct _client_conf_t, tlm_verbose)}, {"client_verbose", BOOL_T,
		offsetof(struct _client_conf_t, client_verbose)}, {"client_print_execution_info", BOOL_T, offsetof(struct _client_conf_t, client_print_execution_info)}, {"client_print_stat", BOOL_T,
		offsetof(struct _client_conf_t, client_print_stat)}, {NULL, NO_TYPE, 0}};

struct parse_table server_params[] = { { "replication_degree", INT_T, offsetof(struct _server_conf_t, replication_degree) }, { "max_servents_per_cpu", INT_T,
		offsetof(struct _server_conf_t, max_servents_per_cpu) }, { "concurrency_control_type", CC_TYPE_T, offsetof(struct _server_conf_t, concurrency_control_type) }, { "locking_timeout", DOUBLE_T,
		offsetof(struct _server_conf_t, locking_timeout) }, { "local_tx_put_cpu_service_demand", DOUBLE_T, offsetof(struct _server_conf_t, local_tx_put_cpu_service_demand) }, {
		"local_tx_get_cpu_service_demand", DOUBLE_T, offsetof(struct _server_conf_t, local_tx_get_cpu_service_demand) }, { "remote_tx_put_cpu_service_demand", DOUBLE_T,
		offsetof(struct _server_conf_t, remote_tx_put_cpu_service_demand) }, { "remote_tx_get_cpu_service_demand", DOUBLE_T, offsetof(struct _server_conf_t, remote_tx_get_cpu_service_demand) }, {
		"tx_begin_cpu_service_demand", DOUBLE_T, offsetof(struct _server_conf_t, tx_begin_cpu_service_demand) }, { "tx_abort_cpu_service_demand", DOUBLE_T,
		offsetof(struct _server_conf_t, tx_abort_cpu_service_demand) }, { "tx_prepare_cpu_service_demand", DOUBLE_T, offsetof(struct _server_conf_t, tx_prepare_cpu_service_demand) }, {
		"tx_send_remote_tx_get_cpu_service_demand", DOUBLE_T, offsetof(struct _server_conf_t, tx_send_remote_tx_get_cpu_service_demand) }, { "local_tx_get_from_remote_cpu_service_demand", DOUBLE_T,
		offsetof(struct _server_conf_t, local_tx_get_from_remote_cpu_service_demand) }, { "remote_tx_get_return_cpu_service_demand", DOUBLE_T,
		offsetof(struct _server_conf_t, remote_tx_get_return_cpu_service_demand) }, { "update_cpu_service_demand", DOUBLE_T, offsetof(struct _server_conf_t, update_cpu_service_demand) }, {
		"local_prepare_successed_cpu_service_demand", DOUBLE_T, offsetof(struct _server_conf_t, local_prepare_successed_cpu_service_demand) }, { "local_prepare_failed_cpu_service_demand", DOUBLE_T,
		offsetof(struct _server_conf_t, local_prepare_failed_cpu_service_demand) }, { "distributed_final_tx_commit_cpu_service_demand", DOUBLE_T,
		offsetof(struct _server_conf_t, distributed_final_tx_commit_cpu_service_demand) }, { "local_tx_final_commit_cpu_service_demand", DOUBLE_T,
		offsetof(struct _server_conf_t, local_tx_final_commit_cpu_service_demand) }, { "tx_prepare_failed_cpu_service_demand", DOUBLE_T,
		offsetof(struct _server_conf_t, tx_prepare_failed_cpu_service_demand) }, { "cpu_queue_length", INT_T, offsetof(struct _server_conf_t, cpu_queue_length) }, { "deadlock_detection_enabled",
		BOOL_T, offsetof(struct _server_conf_t, deadlock_detection_enabled) }, { "cc_print_stat", BOOL_T, offsetof(struct _server_conf_t, cc_print_stat) }, { "print_max_blocked_transactions", BOOL_T,
		offsetof(struct _server_conf_t, print_max_blocked_transactions) }, { "server_verbose", BOOL_T, offsetof(struct _server_conf_t, server_verbose) }, { "tlm_verbose", BOOL_T,
		offsetof(struct _server_conf_t, tlm_verbose) }, { "cc_verbose", BOOL_T, offsetof(struct _server_conf_t, cc_verbose) }, { NULL, NO_TYPE, 0 } };

struct parse_table network_params[] = { { "net_bandwidth", DOUBLE_T, offsetof(struct _network_conf_t, net_bandwidth) },
		{ "avg_packet_size", DOUBLE_T, offsetof(struct _network_conf_t, avg_packet_size) }, { "network_delay", DOUBLE_T, offsetof(struct _network_conf_t, network_delay) }, { "time_for_send_net_req",
				DOUBLE_T, offsetof(struct _network_conf_t, time_for_send_net_req) }, { "time_for_net_deliver", DOUBLE_T, offsetof(struct _network_conf_t, time_for_net_deliver) }, { "net_verbose",
				BOOL_T, offsetof(struct _network_conf_t, net_verbose) }, { NULL, NO_TYPE, 0 } };

/**************************************************/
/******** Convert strings to actual values ********/
/**************************************************/

int *parseArrayInt(const char *val) {

	char *string = (char *) val;
	char *cur_str;
	char *tok = NULL;
	int cur_index = 0;
	int arr_size = 5;
	int *arr;

	// The array is in the format: { val, val, val, ...}.

	// Allocate an initial array
	arr = (int *) malloc(sizeof(int) * arr_size);

	// Now get the tokens
	cur_str = string;
	do {
		tok = strtok(cur_str, "{, }");

		if (cur_str == string) {
			cur_str = NULL; // Continue tokenizing the same string
		}

		// Did we fill the current array?
		if (cur_index == arr_size) {
			arr = realloc(arr, arr_size * 2 * sizeof(int));
			arr_size *= 2;
		}

		if (tok != NULL)
			arr[cur_index++] = parseInt(tok);

	} while (tok != NULL);

	return arr;
}

double *parseArrayDouble(const char *val) {

	char *string = (char *) val;
	char *cur_str;
	char *tok = NULL;
	int cur_index = 0;
	int arr_size = 5;
	double *arr;

	// The array is in the format: { val, val, val, ...}.

	// Allocate an initial array
	arr = (double *) malloc(sizeof(double) * arr_size);

	// Now get the tokens
	cur_str = string;
	do {
		tok = strtok(cur_str, "{, }");

		if (cur_str == string) {
			cur_str = NULL; // Continue tokenizing the same string
		}

		// Did we fill the current array?
		if (cur_index == arr_size) {
			arr = realloc(arr, arr_size * 2 * sizeof(double));
			arr_size *= 2;
		}

		if (tok != NULL)
			arr[cur_index++] = parseDouble(tok);

	} while (tok != NULL);

	return arr;
}

int parseCcType(const char *val) {

	if (strcmp(val, "ETL_2PL") == 0) {
		return ETL_2PL;
	} else if (strcmp(val, "CTL_2PL") == 0) {
		return CTL_2PL;
	} else if (strcmp(val, "PRIMARY_OWNER_CTL_2PL") == 0) {
		return PRIMARY_OWNER_CTL_2PL;
	} else if (strcmp(val, "GMU") == 0) {
		return GMU;
	} else {
		return -1;
	}
}

int parseSystemModel(const char *val) {

	if (strcmp(val, "OPEN") == 0) {
		return OPEN;
	} else if (strcmp(val, "CLOSED") == 0) {
		return CLOSED;
	} else {
		return -1;
	}
}

int parseWorkloadType(const char *val) {

	if (strcmp(val, "SYNTHETIC") == 0) {
		return SYNTHETIC;
	} else if (strcmp(val, "TPCC") == 0) {
		return TPCC;
	} else {
		return -1;
	}
}

int parseDistribution(const char *val) {

	if (strcmp(val, "FIXED") == 0) {
		return FIXED;
	} else if (strcmp(val, "UNIFORM") == 0) {
		return UNIFORM;
	} else if (strcmp(val, "ZIPF") == 0) {
		return ZIPF;
	} else if (strcmp(val, "SINGLE_BAR") == 0) {
		return SINGLE_BAR;
	} else if (strcmp(val, "TOPKEYS") == 0){
		return TOPKEYS;
	} else {
		return -1;
	}
}

/*************************************/
/******** Store configuration ********/
/*************************************/

// This macro allows to easily store parameter's values into structures
// given the field's offset
#define store_param_value(str_ptr, off, type, val) *((type *)((void *)str_ptr + off)) = val;

void parse_value(simulation_conf *configuration, const char *section_name, const char *key, const char *value) {

	int additional_offset = 0;
	int r = 0;
	struct parse_table *p_tab = NULL;

	// Determine which is the correct parsing table to used, depending on the actual parameter section
	if (strcmp(section_name, "Global") == 0) {
		p_tab = global_params;
	} else if (strcmp(section_name, "Client") == 0) {
		p_tab = client_params;
		additional_offset = offsetof(simulation_conf, client_conf);
	} else if (strcmp(section_name, "Server") == 0) {
		p_tab = server_params;
		additional_offset = offsetof(simulation_conf, server_conf);
	} else if (strcmp(section_name, "Network") == 0) {
		p_tab = network_params;
		additional_offset = offsetof(simulation_conf, network_conf);
	} else {
		fprintf(stderr, "Unexpected configuration section: %s\n", section_name);
		return;
	}

	// Now look for a corresponding key
	while (p_tab[r].name != NULL) {
		/*  There are some special cases which must be handled manually, so here we are... */

		if (strcmp(p_tab[r].name, key) == 0) {

			switch (p_tab[r].type) {

			case INT_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, int, parseInt(value));
				break;

			case DOUBLE_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, double, parseDouble(value));
				break;

			case BOOL_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, bool, parseBoolean(value));
				break;

			case CC_TYPE_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, enum _cc_type_t, parseCcType(value));
				break;

			case SYSTEM_MODEL_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, enum _system_model_t, parseSystemModel(value));
				break;

			case WORKLOAD_TYPE_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, enum _workload_type_t, parseWorkloadType(value));
				break;

			case DISTRIBUTION_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, enum _distribution, parseDistribution(value));
				break;

			case ARRAY_INT_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, int *, parseArrayInt(value));
				break;

			case ARRAY_DOUBLE_T:
				store_param_value(configuration, p_tab[r].displacement + additional_offset, double *, parseArrayDouble(value));
				break;

			case NO_TYPE:
			default:
				fprintf(stderr, "In section [%s], unrecognized type for parameter %s\n", section_name, key);
				return;
			}

			// So far so good for this parameter. Go on to the next one!
			return;
		}

		r++;
	}

	fprintf(stderr, "In section [%s], unrecognized parameter: %s\n", section_name, key);
}
#undef store_param_value

/**************************************************/
/****** Main configuration parser starts here *****/
/**************************************************/

/* Strip whitespace chars off end of given string, in place. Return s. */
static char *consume_right(char *s) {
	char *p = s + strlen(s);
	while (p > s && (isspace(*--p) || *p == '\t'))
		*p = '\0';
	return s;
}

/* Return pointer to first non-whitespace char in given string. */
static char *consume_left(const char *s) {
	while (*s && (isspace(*s) || *s == '\t'))
		s++;
	return (char *) s;
}

/* Remve a comment from end of line */
static char *strip_comment(char *s) {
	char *p = s + strlen(s);

	while (--p > s) {
		if (*p == '#')
			*p = '\0';
	}
	return p;
}

/* Find a character in a string */
static char *find_char(const char *s, char c) {
	while (*s && *s != c) {
		s++;
	}
	return (char *) s;
}

/* Version of strncpy that ensures dest (size bytes) is null-terminated. */
static char *strncpy0(char *dest, const char *src, size_t size) {
	strncpy(dest, src, size);
	dest[size - 1] = '\0';
	return dest;
}

/* Parse a given configuration file */
bool configuration_parse(simulation_conf *configuration, const char *filename) {

	// This must be stateful, and is LP-safe since only gid 0 executes the parsing
	static char section_name[MAX_LEN] = "NoSection";

	FILE *file;
	char *start;
	char *end;
	char *key;
	char *value;
	char line[MAX_LINE];

	if ((file = fopen(filename, "r")) == NULL) {
		return false;
	}

	while (fgets(line, MAX_LINE, file) != NULL) {

		start = line;

		// Remove comments and unneeded whitespaces
		start = strip_comment(start);
		start = consume_right(start);
		start = consume_left(start);

		// Empty line
		if (*start == '\0') {
			continue;
		}
		// Ignore comment lines
		if (*start == '#') {
			continue;
		}

		// If a section is found, store its name
		if (*start == '[') {
			strncpy0(section_name, start + 1, strlen(start) - 1);
			continue;
		}

		/* Not a comment, must be a name[=:]value pair */
		end = find_char(start, '=');

		*end = '\0';
		key = consume_right(start);
		value = consume_left(end + 1);

		//if(strcmp(key,"replication_degree")==0) printf("%s = %s\n",key,value);

		// We can now parse what we have found, depending on the actual section we are in!
		parse_value(configuration, section_name, key, value);
	}

	fclose(file);

	//print_configuration(configuration);

	return true;
}

bool configuration_parse_top_keys(simulation_conf *configuration, const char* op_type, const char *filename){
	FILE *f_topkeys;
	char line[MAX_LINE];
	char *tmp_str;
	float *fk_array;
	float *final_fk_array;
	int count_fk = 0;
	int size_array = 5;

	if((f_topkeys = fopen(filename,"r")) == NULL) return false;
	fgets(line,MAX_LINE,f_topkeys);
	fclose(f_topkeys);

	fk_array = (float *) malloc(size_array * sizeof(float));

	for(tmp_str = strtok(line,"{, }") ; tmp_str!=NULL ; tmp_str = strtok(NULL,"{, }")){

		if(count_fk == size_array){
			fk_array = (float *) realloc(fk_array, size_array * 2 *sizeof(float));
			size_array = size_array * 2;
		}

		fk_array[count_fk++] = atof(tmp_str);

	}
	final_fk_array = (float *) malloc((count_fk-1) * sizeof(float));
	memcpy(final_fk_array, fk_array, (count_fk-1) * sizeof(float));

	if(strcmp(op_type,"GETS")==0){
		configuration->client_conf.topkeys_cache_objects_get_probability = final_fk_array;
		configuration->client_conf.number_of_gets_topkeys = count_fk - 1 ;
	}
	else if (strcmp(op_type,"PUTS")==0){
		configuration->client_conf.topkeys_cache_objects_put_probability = final_fk_array;
		configuration->client_conf.number_of_puts_topkeys = count_fk - 1;
	}
	free(fk_array);

	return true;
}

#ifdef DEBUG_CONF
int main(void) {

	simulation_conf configuration;
	configuration_parse(&configuration, "simulation.conf");
	//	print_configuration(&configuration);
	return 0;
}
#endif
