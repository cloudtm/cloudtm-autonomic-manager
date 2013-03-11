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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "states.h"
#include "client/client.h"
#include "server/server.h"
#include "network/net.h"

volatile int x = 1;

// From conf-parser.c
bool configuration_parse(simulation_conf *configuration, const char *filename);

// Tells what kind of Logical Process I am.
// To change the behaviour of LPs, simply modify this function
int who_am_i(int me, state_type *state) {
	if (me < state->num_clients)
		return CLIENT;
	else
		return SERVER;
}

void ProcessEvent(unsigned int me, time_type now, int event_type, event_content_type *event_content, unsigned int size, void *pointer) {

	state_type *state = (state_type *) pointer;
	char *conf_file = NULL, *conf_file_1 = NULL, *conf_file_2 = NULL;
	simulation_conf configuration;
	int i;

	// Upon reception of INIT, install the simulation state
	if (event_type == INIT) {

		// Get the file to load the configuration from
		for (i = 0; i < size; i += 2) {
			if (checkParameter("conf-file", event_content, i)) {
				conf_file = getParameter(event_content, i+1);
			}
			else if(checkParameter("conf-file_1", event_content, i)) {
				conf_file_1 = getParameter(event_content, i+1);
			}
			else if(checkParameter("conf-file_2", event_content, i)) {
				conf_file_2 = getParameter(event_content, i+1);
			}
		}

		// Sanity check
		if (conf_file == NULL) {
			fprintf(stderr, "Configuration file not specified. Please set conf-file before running the simulation.\n");
			exit(-1);
		}

		// Try to load the configuration
		if (!configuration_parse(&configuration, conf_file)) {
			fprintf(stderr, "Error loading configuration file %s\n", conf_file);
			exit(-1);
		}

		if(configuration.client_conf.data_items_access_distribution==TOPKEYS){

			if(!configuration_parse_top_keys(&configuration,"PUTS",conf_file_1)){
				fprintf(stderr, "Error loading puts topkeys' configuration file");
				exit(-1);
			}
			if(!configuration_parse_top_keys(&configuration,"GETS",conf_file_2)){
				fprintf(stderr, "Error loading gets topkeys' configuration file");
				exit(-1);
			}
			/*int x;
			for(x=0; x<configuration.cache_objects;x++){
				if(configuration.client_conf.topkeys_cache_objects_get_probability[x]==0) break;
				printf("%f\n",configuration.client_conf.topkeys_cache_objects_get_probability[x]);
			}*/
		}

		// Allocate simulation state
		state = (state_type *) malloc(sizeof(state_type));
		SetState(state);

		// Store the global configuration
		state->start_stat_time = configuration.start_stat_time;
		state->num_clients = configuration.num_clients;
		state->num_servers = configuration.num_servers;
		state->cache_objects = configuration.cache_objects;
		state->object_replication_degree = configuration.object_replication_degree;
		state->average_server_to_server_net_delay = configuration.average_server_to_server_net_delay;
		state->average_client_to_server_net_delay = configuration.average_client_to_server_net_delay;
		// Now, depending on who I am, store the remainder of the configuration
		switch (who_am_i(me, state)) {

		case CLIENT:
			state->type.client_state.configuration = configuration.client_conf;
			break;

		case SERVER:
			state->type.server_state.configuration = configuration.server_conf;
			break;

		default:
			fprintf(stderr, "Internal error at %s:%d\n", __FILE__, __LINE__);
			break;
		}
	}

	// Dispatch the event to the correct handler
	switch (who_am_i(me, state)) {

	case CLIENT:
		CLIENT_ProcessEvent(me, now, event_type, event_content, size, state);
		break;

	case SERVER:
		SERVER_ProcessEvent(me, now, event_type, event_content, size, state);
		break;

	default:
		fprintf(stderr, "Internal error at %s:%d\n", __FILE__, __LINE__);
		break;
	}

}

// TODO: Alessandro: occorre reimplementare qui il predicato di terminazione!
// TODO: Alessandro: per ora è scritto in modo tale che non termini mai...
int OnGVT(state_type *snapshot, int gid) {

	return false;
}
