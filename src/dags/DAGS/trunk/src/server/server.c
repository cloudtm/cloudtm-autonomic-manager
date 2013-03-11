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
#include "data_distribution_manager.h"
#include "concurrency-control.h"
#include "concurrency-control-functions.h"
#include "cpu.h"

void commit_local_transaction(state_type *state, event_content_type * event_content, time_type now) {
	event_content->applicative_content.op_type = TX_FINAL_LOCAL_COMMIT;
	CC_control(event_content, state, now);
}

void commit_distributed_transaction(state_type *state, event_content_type * event_content, time_type now) {
	//execute local commit
	commit_local_transaction(state, event_content, now);
	//send final commit messages to other servers
	send_final_commit_messages(state, event_content, now);
}

void abort_local_transaction(state_type *state, event_content_type *event_content, time_type now) {
	event_content->applicative_content.op_type = TX_LOCAL_ABORT;
	CC_control(event_content, state, now);
}

void abort_distributed_transaction(state_type *state, event_content_type * event_content, time_type now) {
	//before execute local abort
	abort_local_transaction(state, event_content, now);
	//send final abort messages to other servers
	send_final_abort_messages(state, event_content, now);
}

//function per il processamento degli eventi da parte del server
void process_message(int me, time_type now, event_content_type * event_content, state_type *state) {

	// Get the server configuration from simulation state
	SERVER_lp_state_type *pointer = &state->type.server_state;

	// check operation type
	switch (event_content->applicative_content.op_type) {

	case TX_BEGIN:
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_BEGIN  at time %f for transaction %d\n", me, now, event_content->applicative_content.tx_id);
		//add transaction metadata to active_transaction list
		int ret = add_transaction_metadata(event_content->applicative_content.tx_id, 1, event_content->applicative_content.tx_run_number, pointer);
		int cc_response = CC_control(event_content, state, now);
		if (cc_response == 1) {
			event_content->applicative_content.op_type = CPU_TX_BEGIN;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_TX_BEGIN added to CPU at time %f\n", me, now);
		} else if (cc_response == -1) {
			event_content->applicative_content.op_type = CPU_TX_LOCAL_ABORT;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_TX_ABORT added to CPU at time %f\n", me, now);
		}
		break;

	case TX_GET: //get received from a client
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_GET  at time %f\n", me, now);
		//am I the owner of the key?
		if (is_owner(event_content->applicative_content.object_key_id, me, state->num_servers, state->num_clients, state->object_replication_degree)) {
			//..yes, call the concurrency control
			int cc_response = CC_control(event_content, state, now);
			if (cc_response == 1) {
				//operation allowed
				event_content->applicative_content.op_type = CPU_TX_LOCAL_GET;
				add_processing_request(pointer, event_content, now);
				if (pointer->configuration.server_verbose)
					printf("S%d - CPU_LOCAL_TX_GET added to CPU at time %f\n", me, now);
			} else if (cc_response == -1) {
				//operation not allowed, transaction has to be aborted
				if (pointer->configuration.server_verbose)
					printf("S%d - Server_ProcessEvent: operation aborted by the concurrency control aat time %f\n", me, now);
				event_content->applicative_content.op_type = CPU_TX_LOCAL_ABORT;
				add_processing_request(pointer, event_content, now);
				if (pointer->configuration.server_verbose)
					printf("S%d - CPU_TX_ABORT added to CPU at time %f\n", me, now);
			}
		} else {
			//..no (I'not the owner), TX_GET has to be sent to remote server(s)
			event_content->applicative_content.op_type = CPU_TX_SEND_REMOTE_GET;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_SEND_REMOTE_TX_GET added to CPU at time %f\n", me, now);
		}
		break;

	case TX_REMOTE_GET: //get received from a server
		if (pointer->configuration.server_verbose)
			printf("S%d - Server_ProcessEvent: TX_REMOTE_GET received from server %i for operation %i of transaction %i at time %f\n", me, pointer->server_id,
					event_content->applicative_content.op_number, event_content->applicative_content.tx_id, now);
		event_content->applicative_content.op_type = CPU_TX_LOCAL_GET_FROM_REMOTE;
		add_processing_request(pointer, event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - CPU_LOCAL_TX_GET_FROM_REMOTE added to CPU at time %f\n", me, now);
		break;

	case TX_REMOTE_GET_RETURN: //get response received from a server
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_REMOTE_GET_RETURN from server %i for operation %i della txn %i at time %f\n", me, pointer->server_id,
					event_content->applicative_content.op_number, event_content->applicative_content.tx_id, now);
		event_content->applicative_content.op_type = CPU_TX_REMOTE_GET_RETURN;
		add_processing_request(pointer, event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - CPU_REMOTE_TX_GET_RETURN added to CPU at time %f\n", me, now);
		break;

	case TX_PUT: //put received from a client
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_PUT (txn %i, op: %i)  at time %f\n", me, event_content->applicative_content.tx_id, event_content->applicative_content.op_number,
					now);
		//call the concurrency control
		cc_response = CC_control(event_content, state, now);
		if (cc_response == 1) {
			//operation allowed
			event_content->applicative_content.op_type = CPU_TX_LOCAL_PUT;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_LOCAL_TX_PUT added to CPU at time %f\n", me, now);
		} else if (cc_response == -1) {
			//operation not not allowed, transaction has to be aborted
			event_content->applicative_content.op_type = CPU_TX_LOCAL_ABORT;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_TX_ABORT added to CPU at time %f\n", me, now);
		}
		break;

	case TX_UPDATE: //data update update received from a primary owner server
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received UPDATE  at time %f \n", me, now);
		event_content->applicative_content.op_type = CPU_TX_UPDATE;
		add_processing_request(pointer, event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - CPU_UPDATE added to CPU at time %f\n", me, now);
		break;

	case TX_COMMIT: // commit request received from a client
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_COMMIT for transaction %i  at time %f \n", me, event_content->applicative_content.tx_id, now);
		cc_response = CC_control(event_content, state, now);
		if (cc_response == 1) {
			//operation allowed
			transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			if (transaction == NULL) {
				//CC_free_cache_locks(event_content, pointer, now);
				return;
			}
			event_content->applicative_content.op_type = CPU_PREPARE;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_PREPARE for transaction %i added to CPU at time %f\n", me, event_content->applicative_content.tx_id, now);

		} else if (cc_response == -1) {
			////operation not allowed, transaction has to be aborted
			if (pointer->configuration.server_verbose)
				printf("S%d - function Server_ProcessEvent: operazione abortita dal Controllore di Concorrenza at time %f\n", me, now);
			event_content->applicative_content.op_type = CPU_TX_LOCAL_ABORT;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_TX_ABORT added to CPU at time %f\n", me, now);
		}
		break;

	case TX_PREPARE: //prepare request received from a coordinator server
		ret = add_transaction_metadata(event_content->applicative_content.tx_id, 0, event_content->applicative_content.tx_run_number, pointer);
		if (ret == -1 && pointer->configuration.cc_verbose)
			printf("[cc%d] ATTENZIONE: ERRORE NELL'INSERIRE LA TRANSAZIONE %d NEL' ACTIVE TRANSACTION TABLE\n", pointer->server_id, event_content->applicative_content.tx_id);
		if (pointer->configuration.server_verbose)
			printf(" S%d - function Server_ProcessEvent: received TX_PREPARE for transaction %i at time %f \n", me, event_content->applicative_content.tx_id, now);
		int CC_response = CC_control(event_content, state, now);
		if (CC_response == 1) {
			event_content->applicative_content.op_type = CPU_TX_LOCAL_PREPARE_SUCCESSED;
			add_processing_request(pointer, event_content, now);
			remove_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_LOCAL_PREPARE_SUCCESSED added to CPU at time %f\n", me, now);
		} else if (CC_response == -1) {
			event_content->applicative_content.op_type = CPU_TX_LOCAL_PREPARE_FAILED;
			add_processing_request(pointer, event_content, now);
			remove_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_LOCAL_PREPARE_FAILED added to CPU at time %f\n", me, now);
		}
		break;

	case TX_PREPARE_SUCCEEDED: // received from a remote participant server
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_PREPARE_SUCCEEDED for transaction %i  at time %f \n", me, event_content->applicative_content.tx_id, now);
		transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		// decrement expected response counter
		if (transaction != NULL && transaction->current_tx_run_number == event_content->applicative_content.tx_run_number) {
			transaction->expected_prepare_response_counter--;
			if (transaction->expected_prepare_response_counter == 0) {
				//all expected prepare messages have been succesfully received, transaction can commit
				event_content->applicative_content.op_type = CPU_TX_DISTRIBUTED_FINAL_COMMIT;
				add_processing_request(pointer, event_content, now);
				if (pointer->configuration.server_verbose)
					printf("S%d - CPU_DISTRIBUTED_FINAL_TX_COMMIT to cpu of transaction %i at time %f\n", me, event_content->applicative_content.tx_id, now);
			}
		}
		break;

	case TX_PREPARE_FAILED: // received from a remote participant server
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_PREPARE_FAILED for transaction %i at time %f\n", me, event_content->applicative_content.tx_id, now);
		event_content->applicative_content.op_type = CPU_TX_PREPARE_FAILED;
		add_processing_request(pointer, event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - CPU_TX_PREPARE_FAILED added to CPU at time %f\n", me, now);
		break;

	case TX_DISTRIBUTED_FINAL_COMMIT: //received from a remote coordinator server
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_DISTRIBUTED_FINAL_COMMIT for transaction %i at time %f \n", me, event_content->applicative_content.tx_id, now);
		event_content->applicative_content.op_type = CPU_TX_LOCAL_FINAL_COMMIT;
		add_processing_request(pointer, event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - CPU_LOCAL_TX_FINAL_COMMIT for transaction %i added to CPU at time %f\n", me, event_content->applicative_content.tx_id, now);
		break;

	case TX_REMOTE_ABORT: // received from a remote coordinator server
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_REMOTE_ABORT at time %f for transaction %d\n", me, now, event_content->applicative_content.tx_id);

		event_content->applicative_content.op_type = CPU_TX_REMOTE_ABORT;
		add_processing_request(pointer, event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - CPU_TX_REMOTE_ABORT added to CPU at time %f\n", me, now);
		break;

	default:
		printf("ERRORE: event type %i not managed\n", event_content->applicative_content.op_type);
		break;
	}
}

void SERVER_ProcessEvent(unsigned int me, time_type now, int event_type, event_content_type *event_content, unsigned int size, state_type *state) {

	// get the server configuration from simulation state
	SERVER_lp_state_type *pointer = &state->type.server_state;
	transaction_metadata *transaction;

	switch (event_type) {

	// Configuration has just been stored
	case INIT:
		if (pointer->configuration.server_verbose) {
			printf("S%d - function Server_ProcessEvent: received SHARE_CONF\n", me);
		}
		pointer->server_id = me;
		pointer->stored_values = 0;
		concurrency_control_init(state);
		cpu_init(pointer);

		break;

	case CPU_TX_LOCAL_GET: //return from cpu in case of local get
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received READ_LOCAL_VALUE_RET_CPU  at time %f\n", me, now);
		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction == NULL) {
			printf("ERROR: no transaction found with id %d (from client id %d)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			exit(-1);
		}
		transaction->executed_operations++;
		event_content_type new_event_content;
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		new_event_content.applicative_content.object_key_id = event_content->applicative_content.object_key_id;
		new_event_content.destination_object_id = event_content->applicative_content.client_id;
		new_event_content.applicative_content.op_type = TX_GET_RETURN;
		server_send_message(state, &new_event_content, now);
		break;

	case CPU_TX_LOCAL_PUT: //return from cpu in case of local put
		if (pointer->configuration.server_verbose)
			printf("- function Server_ProcessEvent: received WRITE_VALUE_RET_CPU  at time %f\n", now);
		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction == NULL) {
			printf("ERROR: no transaction found with id %d (from client id %d)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			exit(-1);
		}
		transaction->executed_operations++;
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		new_event_content.applicative_content.server_id = pointer->server_id;
		new_event_content.applicative_content.op_type = TX_PUT_RETURN;
		new_event_content.destination_object_id = event_content->applicative_content.client_id;
		server_send_message(state, &new_event_content, now);
		if (pointer->configuration.server_verbose)
			printf("- function Server_ProcessEvent: sent DELIVER_PUT_RETURN to NET at time %f\n", now);
		break;

	case CPU_TX_REMOTE_GET_RETURN: //return from cpu in case of remote get
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: CPU_REMOTE_TX_GET_RETURN received  at time %f\n", me, now);
		// check if a remote get response has already arrived
		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction == NULL) {
			break;
		}
		if (event_content->applicative_content.op_number - 1 == transaction->executed_operations) {
			transaction->executed_operations++;
			memcpy(&new_event_content, event_content, sizeof(event_content_type));
			new_event_content.applicative_content.object_key_id = event_content->applicative_content.object_key_id;
			new_event_content.destination_object_id = new_event_content.applicative_content.client_id;
			new_event_content.applicative_content.op_type = TX_GET_RETURN;
			server_send_message(state, &new_event_content, now);
		}
		break;

	case CPU_PREPARE: //return from cpu in case of prepare
		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		transaction->expected_prepare_response_counter = 0;
		//send prepare messages to other servers
		send_prepare_messages(state, transaction, event_content, now);
		// check whether not to wait for other server response
		if (transaction->expected_prepare_response_counter == 0) {
			// can immediately commit
			if (pointer->configuration.server_verbose)
				printf("S%d - la tx %i ha solo write locali\n", me, event_content->applicative_content.tx_id);
			event_content->applicative_content.op_type = CPU_TX_DISTRIBUTED_FINAL_COMMIT;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_DISTRIBUTED_FINAL_TX_COMMIT to cpu for transaction %i at time %f\n", me, event_content->applicative_content.tx_id, now);
		}
		break;

	case CPU_TX_LOCAL_PREPARE_SUCCESSED: //return from cpu in case of local prepare
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		new_event_content.applicative_content.op_type = TX_PREPARE_SUCCEEDED;
		new_event_content.destination_object_id = new_event_content.applicative_content.server_id;
		server_send_message(state, &new_event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: sent TX_PREPARE_SUCCEEDED for transaction %i at time %f to server %i\n", me, event_content->applicative_content.tx_id, now,
					new_event_content.applicative_content.server_id);
		break;

	case CPU_TX_LOCAL_PREPARE_FAILED:
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		new_event_content.applicative_content.op_type = TX_PREPARE_FAILED;
		new_event_content.destination_object_id = new_event_content.applicative_content.server_id;
		server_send_message(state, &new_event_content, now);
		if (pointer->configuration.server_verbose)
			printf(" S%d - function Server_ProcessEvent: sent TX_PREPARE_FAILED for transaction %i at time %f to server %i\n", me, event_content->applicative_content.tx_id, now,
					new_event_content.applicative_content.server_id);
		break;

	case CPU_TX_PREPARE_FAILED:
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received CPU_PREPARE_FAILED  for transaction %i at time %f \n", me, event_content->applicative_content.tx_id, now);
		// transaction must abort
		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction != NULL && transaction->current_tx_run_number == event_content->applicative_content.tx_run_number) {
			abort_distributed_transaction(state, event_content, now);
			remove_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			if (pointer->configuration.server_verbose)
				printf("S%d - function Server_ProcessEvent: transazione abortita at time %f\n", me, now);
			//reply to client
			event_content->destination_object_id = event_content->applicative_content.client_id;
			event_content->applicative_content.op_type = TX_EXECUTION_EXCEPTION;
			server_send_message(state, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - sent TX_EXECUTION_EXCEPTION for transaction %i to client %i at time %f\n", me, event_content->applicative_content.tx_id, event_content->applicative_content.client_id,
						now);
		}
		break;

	case CPU_TX_DISTRIBUTED_FINAL_COMMIT:
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received CPU_DISTRIBUTED_FINAL_TX_COMMIT  at time %f for transaction %i\n", me, now, event_content->applicative_content.tx_id);
		commit_distributed_transaction(state, event_content, now);
		remove_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		//reply to client
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		new_event_content.applicative_content.op_type = TX_COMMIT_RESPONSE;
		new_event_content.destination_object_id = new_event_content.applicative_content.client_id;
		server_send_message(state, &new_event_content, now);
		break;

	case CPU_TX_LOCAL_FINAL_COMMIT:
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received CPU_LOCAL_TX_FINAL_COMMIT  at time %f for transaction %i\n", me, now, event_content->applicative_content.tx_id);
		commit_remote_transaction(state, event_content, now);
		break;

	case TX_LOCAL_TIMEOUT: // timeout for a local transaction
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_LOCAL_TIMEOUT at time %f for transaction %d\n", me, now, event_content->applicative_content.tx_id);
		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction != NULL && transaction->current_tx_run_number == event_content->applicative_content.tx_run_number && transaction->is_blocked) {
			event_content->applicative_content.op_type = TX_LOCAL_ABORT;
			CC_control(event_content, state, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - function Server_ProcessEvent: transaction aborted at time %f\n", me, now);
			remove_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			event_content->applicative_content.op_type = CPU_TX_LOCAL_ABORT;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_TX_ABORT added to CPU at time %f\n", me, now);
		}
		break;

	case TX_PREPARE_TIMEOUT: // timeout for a prepare of a remote transaction
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_PREPARE_TIMEOUT at time %f for transaction %d\n", me, now, event_content->applicative_content.tx_id);
		// remove waiting event of transaction
		remove_event_of_tx(pointer->cc_metadata->event_queue, event_content->applicative_content.tx_id);
		transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction != NULL && transaction->current_tx_run_number == event_content->applicative_content.tx_run_number && transaction->is_blocked) {
			remove_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			//reply to server
			memcpy(&new_event_content, event_content, sizeof(event_content_type));
			new_event_content.destination_object_id = event_content->applicative_content.server_id;
			new_event_content.applicative_content.op_type = TX_PREPARE_FAILED;
			server_send_message(state, &new_event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - Server_ProcessEvent: transaction %i aborted at time %f per LOCK_TIMEOUT\n", me, event_content->applicative_content.tx_id, now);
			event_content->applicative_content.op_type = CPU_TX_REMOTE_ABORT;
			add_processing_request(pointer, event_content, now);
			if (pointer->configuration.server_verbose)
				printf("S%d - CPU_TX_REMOTE_ABORT added to CPU at time %f\n", me, now);
		}
		break;

	case CPU_TX_BEGIN: //return from cpu in case of begin
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received TX_BEGIN_RET_CPU  at time %f\n", me, now);
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		new_event_content.destination_object_id = event_content->applicative_content.client_id;
		new_event_content.applicative_content.op_type = TX_BEGIN_RETURN;
		server_send_message(state, &new_event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - Server_ProcessEvent: TX_BEGIN_RETURN sent at time %f\n", me, now);
		break;

	case CPU_TX_LOCAL_ABORT: //return from cpu in case of local transaction abort
		if (pointer->configuration.server_verbose)
			printf("S%d - tx %i abortita at time %f\n", me, event_content->applicative_content.tx_id, now);
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		//reply to client
		new_event_content.destination_object_id = event_content->applicative_content.client_id;
		new_event_content.applicative_content.op_type = TX_EXECUTION_EXCEPTION;
		server_send_message(state, &new_event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - TX_EXECUTION_EXCEPTION of transaction %i sent to client %i at time %f\n", me, event_content->applicative_content.tx_id, event_content->applicative_content.client_id, now);
		break;

	case CPU_TX_REMOTE_ABORT: //return from cpu in case of remote transaction abort
		abort_remote_transaction(state, event_content, now);
		break;

	case CPU_TX_LOCAL_GET_FROM_REMOTE: //return from cpu in case of local get from a remote transaction
		//send get response to the server
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: received CPU_LOCAL_TX_GET_FROM_REMOTE  at time %f\n", me, now);
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		new_event_content.destination_object_id = event_content->applicative_content.server_id;
		new_event_content.applicative_content.op_type = TX_REMOTE_GET_RETURN;
		server_send_message(state, &new_event_content, now);
		if (pointer->configuration.server_verbose)
			printf("S%d - Server_ProcessEvent: DELIVER_VALUE sent to NET at time %f\n", me, now);
		break;

	case CPU_TX_SEND_REMOTE_GET: //return from cpu in case of response to a remote get request
		if (pointer->configuration.server_verbose)
			printf("S%d - function Server_ProcessEvent: CPU_SEND_REMOTE_TX_GET received at time %f\n", me, now);
		send_remote_tx_get(state, event_content, now);
		break;

	case CPU_PROCESSING_REQUEST_EXECUTED: //notify a request completion to cpu
		cpu_request_executed(pointer, now);
		break;

	case DELIVER_MESSAGE: // a message from network has been received...
		process_message(me, now, event_content, state);
		break;

	default:
		printf("ERROR: event type %i not managed\n", event_type);
		exit(-1);
		break;
	}
}

