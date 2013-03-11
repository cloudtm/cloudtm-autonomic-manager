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
#include <string.h>
#include "../network/net.h"
#include "hashing.h"
#include "concurrency-control.h"
#include "concurrency-control-functions.h"
#include "cpu.h"

void server_send_message(state_type *state, event_content_type *message, time_type now) {
	// Get the server configuration from simulation state
	SERVER_lp_state_type *pointer = &state->type.server_state;
	message->origin_object_id = pointer->server_id;

	net_send_message(message->origin_object_id, message->destination_object_id, message, state->num_clients, state->num_servers, now, state->average_server_to_server_net_delay);

	if (pointer->configuration.server_verbose)
		printf("S%d - function Server_ProcessEvent: DELIVER_MESSAGE sent at time %f\n", pointer->server_id, now);
}

void send_remote_tx_get(state_type *state, event_content_type * event_content, time_type now) {
	int s;
	// Get the server configuration from simulation state
	SERVER_lp_state_type *pointer = &state->type.server_state;
	for (s = state->num_clients; s < state->num_clients + state->num_servers; s++) {
		if (s != pointer->server_id && is_owner(event_content->applicative_content.object_key_id, s, state->num_servers, state->num_clients, state->object_replication_degree)) {
			event_content_type new_event_content_rem;
			memcpy(&new_event_content_rem, event_content, sizeof(event_content_type));
			new_event_content_rem.applicative_content.object_key_id = event_content->applicative_content.object_key_id;
			new_event_content_rem.applicative_content.owner_id = s;
			new_event_content_rem.destination_object_id = s;
			new_event_content_rem.applicative_content.op_type = TX_REMOTE_GET;
			server_send_message(state, &new_event_content_rem, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - function Server_ProcessEvent: TX_REMOTE_GET sent at time %f\n", pointer->server_id, now);
		}
	}
}

//send prepare messages to other servers
void send_prepare_messages(state_type *state, transaction_metadata *transaction, event_content_type * event_content, time_type now) {
	int s;
	// Get the server configuration from simulation state
	SERVER_lp_state_type *pointer = &state->type.server_state;
	for (s = state->num_clients; s < state->num_clients + state->num_servers; s++) {
		int a_server_founded = 0;
		data_set_entry *entry = transaction->write_set;
		while (entry != NULL && !a_server_founded) {
			if (pointer->configuration.concurrency_control_type == PRIMARY_OWNER_CTL_2PL) {
				if (is_primary(entry->object_key_id, s, state->num_servers, state->num_clients)) {
					a_server_founded = 1;
				}
			} else {
				if (is_owner(entry->object_key_id, s, state->num_servers, state->num_clients, state->object_replication_degree)) {
					a_server_founded = 1;
				}
			}
			entry = entry->next;
		}
		if (s != event_content->applicative_content.server_id && a_server_founded) {
			event_content_type new_event_content;
			memcpy(&new_event_content, event_content, sizeof(event_content_type));
			new_event_content.applicative_content.op_type = TX_PREPARE;
			new_event_content.destination_object_id = s;
			new_event_content.applicative_content.write_set = transaction->write_set;
			transaction->expected_prepare_response_counter++;
			server_send_message(state, &new_event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - function Server_ProcessEvent: TX_PREPARE sent at time %f to server %i\n", event_content->applicative_content.server_id, now, s);
		}
	}
}

//send final abort messages to other servers
void send_final_abort_messages(state_type *state, event_content_type * event_content, time_type now) {
	int s;
	// Get the server configuration from simulation state
	SERVER_lp_state_type *pointer = &state->type.server_state;
	transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
	if (transaction == NULL) {
		printf("ERROR: no transaction found with id %d (from client id %d)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
		exit(-1);
	}
	for (s = state->num_clients; s < state->num_clients + state->num_servers; s++) {
		// send a message to other servers containing at least an entry of the transaction write set
		int an_entry_founded = 0;
		data_set_entry *entry = transaction->write_set;
		while (entry != NULL && !an_entry_founded) {
			if (pointer->configuration.concurrency_control_type == PRIMARY_OWNER_CTL_2PL) {
				if (is_primary(entry->object_key_id, s, state->num_servers, state->num_clients))
					an_entry_founded = 1;
			} else {
				if (is_owner(entry->object_key_id, s, state->num_servers, state->num_clients, state->object_replication_degree))
					an_entry_founded = 1;
			}
			entry = entry->next;
		}
		if (s != event_content->applicative_content.server_id && an_entry_founded) {
			event_content->applicative_content.op_type = TX_REMOTE_ABORT;
			event_content->destination_object_id = s;
			event_content->applicative_content.write_set = transaction->write_set;
			transaction->expected_prepare_response_counter++;
			server_send_message(state, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - function Server_ProcessEvent: TX_REMOTE_ABORT sent at time %f to server %i\n", event_content->applicative_content.server_id, now, s);
		}
	}
}

//send prepare messages to other servers
void send_final_commit_messages(state_type *state, event_content_type * event_content, time_type now) {
	int s;
	// Get the server configuration from simulation state
	SERVER_lp_state_type *pointer = &state->type.server_state;
	transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
	if (transaction == NULL) {
		printf("ERROR: no transaction fouded with id %d (from cliendt %d)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
		exit(-1);
	}
	for (s = state->num_clients; s < state->num_clients + state->num_servers; s++) {
		int a_server_founded = 0;
		data_set_entry *entry = transaction->write_set;
		while (entry != NULL && !a_server_founded) {
			if (pointer->configuration.concurrency_control_type == PRIMARY_OWNER_CTL_2PL) {
				if (is_primary(entry->object_key_id, s, state->num_servers, state->num_clients))
					a_server_founded = 1;
			} else {
				if (is_owner(entry->object_key_id, s, state->num_servers, state->num_clients, state->object_replication_degree))
					a_server_founded = 1;
			}
			entry = entry->next;
		}
		if (s != event_content->applicative_content.server_id && a_server_founded) {
			event_content_type new_event_content;
			memcpy(&new_event_content, event_content, sizeof(event_content_type));
			new_event_content.applicative_content.op_type = TX_DISTRIBUTED_FINAL_COMMIT;
			new_event_content.destination_object_id = s;
			new_event_content.applicative_content.write_set = transaction->write_set;
			transaction->expected_prepare_response_counter++;
			server_send_message(state, &new_event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - function Server_ProcessEvent: TX_DISTRIBUTED_FINAL_COMMIT sent at time %f to server %i\n", event_content->applicative_content.server_id, now, s);
		}
	}
}

void commit_remote_transaction(state_type *state, event_content_type * event_content, time_type now) {
	event_content->applicative_content.op_type = TX_FINAL_REMOTE_COMMIT;
	CC_control(event_content, state, now);
}

void abort_remote_transaction(state_type *state, event_content_type *event_content, time_type now) {
	event_content->applicative_content.op_type = TX_REMOTE_ABORT;
	CC_control(event_content, state, now);
}

