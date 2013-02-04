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
#include "../ROOT-Sim.h"
#include "../states.h"
#include "transaction_list_manager.h"
#include "../network/net.h"

//return the server id
int get_server(state_type *state) {
	return RandomRange(0, state->num_servers - 1) + state->num_clients;
}

void print_final_statistics(state_type *state, time_type now) {
	// get the client configuration from simulation state
	CLIENT_lp_state_type *pointer = &state->type.client_state;
	if (pointer->client_id == 0) {
		int class_id;
		double abort_ratio, response_time;
		for (class_id = 0; class_id < pointer->configuration.number_of_tx_classes; class_id++) {
			if (pointer->tx_classes[class_id]->tot_tx_num == 0)
				abort_ratio = 0;
			else
				abort_ratio = (double) pointer->tx_classes[class_id]->aborted_tx_num / (double) pointer->tx_classes[class_id]->tot_tx_num;
			if (pointer->tx_classes[class_id]->committed_tx_num == 0)
				response_time = 0;
			else
				response_time = (double) pointer->tx_classes[class_id]->tot_response_time / (double) pointer->tx_classes[class_id]->committed_tx_num;
			printf("%i,%i,%i,%i,%f,%f,%f", state->num_clients, state->num_servers, state->object_replication_degree, class_id, response_time, pointer->tx_classes[class_id]->tot_tx_num / now,
					abort_ratio);
			/*
			 printf("%f",
			 response_time
			 );
			 */
		}
	}
}

void run_started(int tx_run_number, CLIENT_lp_state_type * pointer, double now) {
	pointer->active_transactions++;
	pointer->started_runs++;

	if (tx_run_number > 0) {
		pointer->in_backoff_transactions--;
	}
}

//execute the next operation of a transaction
operation_descriptor *next_operation_request(int tx_id, int tx_run_number, time_type delay, state_type *state, time_type now) {

	// get the client configuration from simulation state
	CLIENT_lp_state_type *pointer = &state->type.client_state;

	event_content_type new_event_content;

	transaction_descriptor * tx_descr = get_transaction(tx_id, pointer->transaction_list);
	operation_descriptor * op_descr;

	if (tx_descr != NULL) {
		op_descr = get_next_operation(tx_descr);
	} else {
		return NULL;
	}

	if (op_descr != NULL) {
		// send the next operation to server
		new_event_content.applicative_content.tx_id = tx_id;
		new_event_content.applicative_content.tx_run_number = tx_run_number;
		new_event_content.applicative_content.tx_class_id = tx_descr->tx_class_id;
		new_event_content.applicative_content.op_number = op_descr->op_number;
		new_event_content.applicative_content.object_key_id = op_descr->object_key_id;
		new_event_content.applicative_content.op_type = op_descr->op_type;
		new_event_content.applicative_content.client_id = pointer->client_id;
		new_event_content.applicative_content.server_id = tx_descr->server_id;
		new_event_content.origin_object_id = pointer->client_id;
		new_event_content.destination_object_id = tx_descr->server_id;

		net_send_message(pointer->client_id, tx_descr->server_id, &new_event_content, state->num_clients, state->num_servers, now + delay, 0);

		if (pointer->configuration.client_verbose) {
			printf("C%i - inviato evento %i (TX_PUT|TX_GET) al tempo %f, operazione num %i della transazione %i\n", pointer->client_id, op_descr->op_type, now + delay, op_descr->op_number, tx_id);
		}
		return op_descr;
	} else {
		// all operations of the transaction have already been executed...
		return NULL;
	}
}

//create a new transaction
transaction_descriptor *new_transaction(state_type *state, double now) {

	// get the client configuration from simulation state
	CLIENT_lp_state_type *pointer = &state->type.client_state;

	if (pointer->configuration.workload_type == SYNTHETIC) {

		transaction_descriptor *tx = create_new_synthetic_transaction(state, &pointer->transaction_list, pointer->next_transaction_id++, (double) pointer->configuration.data_items_zipf_const);
		tx->server_id = get_server(state);

		return tx;
	} else if (pointer->configuration.workload_type == FROM_TRACE) {
		return NULL;
		//		return create_new_transaction_from_trace(&pointer->transaction_list, pointer->next_transaction_id++, pointer->configuration.data_items_access_distribution, (double)pointer->configuration.data_items_zipf_const);
	}

	return NULL;
}

//run a transaction
transaction_descriptor * transaction_run_request(transaction_descriptor *tx_descr, int run_number, time_type delay, state_type *state, time_type now) {

	// get the client configuration from simulation state
	CLIENT_lp_state_type *pointer = &state->type.client_state;
	event_content_type new_event_content;
	new_event_content.applicative_content.tx_id = tx_descr->tx_id;
	new_event_content.applicative_content.tx_class_id = tx_descr->tx_class_id;
	new_event_content.applicative_content.tx_run_number = run_number;
	new_event_content.applicative_content.client_id = pointer->client_id;
	new_event_content.applicative_content.op_type = TX_BEGIN;
	new_event_content.applicative_content.op_number = 0;
	new_event_content.applicative_content.client_id = pointer->client_id;
	new_event_content.applicative_content.server_id = tx_descr->server_id;
	new_event_content.destination_object_id = tx_descr->server_id;
	new_event_content.origin_object_id = pointer->client_id;

	net_send_message(pointer->client_id, tx_descr->server_id, &new_event_content, state->num_clients, state->num_servers, now + delay, 0);

	if (pointer->configuration.client_verbose) {
		printf("C%i - inviato evento TX_BEGIN  al tempo %f, operazione num %i della transazione %i\n", pointer->client_id, now, 0, tx_descr->tx_id);
	}
	run_started(run_number, pointer, now);
	return tx_descr;
}

//send the commit request to server for transaction tx_id
void commit_request(int tx_id, int tx_run_number, time_type delay, time_type now, state_type *state, event_content_type *message) {
	// get the client configuration from simulation state
	CLIENT_lp_state_type *pointer = &state->type.client_state;

	transaction_descriptor * tx_descr = get_transaction(tx_id, pointer->transaction_list);

	event_content_type new_event_content;
	memcpy(&new_event_content, message, sizeof(event_content_type));
	new_event_content.applicative_content.tx_id = tx_descr->tx_id;
	new_event_content.applicative_content.tx_run_number = tx_run_number;
	new_event_content.destination_object_id = tx_descr->server_id;
	new_event_content.origin_object_id = pointer->client_id;
	new_event_content.applicative_content.op_type = TX_COMMIT;

	net_send_message(pointer->client_id, tx_descr->server_id, &new_event_content, state->num_clients, state->num_servers, now + delay, 0);

	if (pointer->configuration.client_verbose) {
		printf("C%i - inviato evento COMMIT_REQUEST a TM con tx_id %i al tempo %f\n", pointer->client_id, tx_descr->tx_id, now);
	}
}

void CLIENT_ProcessEvent(unsigned int me, double now, int event_type, event_content_type *event_content, unsigned int size, state_type *state) {
	event_content_type new_event_content;
	transaction_descriptor * tx_descr;
	operation_descriptor * next_op;
	double delay;
	int i;

	// get the client configuration from simulation state
	CLIENT_lp_state_type *pointer = &state->type.client_state;

	//switch on basis of event_type
	switch (event_type) {

	case INIT:

		pointer->tx_classes = (tx_class_statistics **) malloc(pointer->configuration.number_of_tx_classes * sizeof(tx_class_statistics *));
		int cl;
		for (cl = 0; cl < pointer->configuration.number_of_tx_classes; cl++) {
			memset(pointer->tx_classes, 0, sizeof(tx_class_statistics));
			pointer->tx_classes[cl] = (tx_class_statistics *) malloc(sizeof(tx_class_statistics));
			pointer->tx_classes[cl]->class_id = cl;
			pointer->tx_classes[cl]->aborted_tx_num = 0;
			pointer->tx_classes[cl]->committed_tx_num = 0;
			pointer->tx_classes[cl]->tot_response_time = 0.0;
			pointer->tx_classes[cl]->tot_tx_num = 0;
		}

		//initialize client state
		if (pointer->configuration.client_verbose) {
			printf("C%i - ricevuto evento SHARE_CONF\n", pointer->client_id);
		}

		// reset initial values
		pointer->client_id = me;
		pointer->next_transaction_id = pointer->configuration.number_of_transactions * pointer->client_id;
		pointer->aborted_runs = 0;
		pointer->started_runs = 0;
		pointer->committed_transactions = 0;
		pointer->executed_get_operation = 0;
		pointer->executed_put_operation = 0;
		pointer->active_transactions = 0;
		pointer->in_backoff_transactions = 0;
		pointer->transaction_list = NULL;
		pointer->ending = 0;
		pointer->total_transaction_execution_time = 0;

		if (pointer->configuration.system_model == CLOSED) {
			for (i = 0; i < pointer->configuration.number_of_threads; i++) {
				if (pointer->committed_transactions < pointer->configuration.number_of_transactions) {
					tx_descr = new_transaction(state, now);

					if (pointer->configuration.workload_type == 0) {
						delay = Expent(((double) pointer->configuration.inter_tx_think_time));
					} else {
						delay = tx_descr->previous_ntbc;
					}
					tx_descr->start_time = delay;
					tx_descr->last_run_start_time = delay;

					transaction_run_request(tx_descr, 0, delay, state, now);
				}
			}
		}

		if (pointer->configuration.system_model == OPEN) {
			// schedule the first transaction execution request
			delay = Expent(((double) 1 / (double) pointer->configuration.tx_arrival_rate));
			ScheduleNewEvent(me, now + delay, RUN_NEW_TX, &new_event_content, sizeof(event_content_type));
		}

		break;

	// manage received messagges
	case DELIVER_MESSAGE:

		switch (event_content->applicative_content.op_type) {

		case TX_BEGIN_RETURN:
		case TX_PUT_RETURN:
		case TX_GET_RETURN:
			if (pointer->configuration.client_verbose) {
				printf("C%i - ricevuto evento %i (TX_ BEGIN|GET|PUT _RETURN) per op %i della tx %i al tempo %f\n", me, event_content->applicative_content.op_type,
						event_content->applicative_content.op_number, event_content->applicative_content.tx_id, now);
			}
			// send the next operation request, if any, to server...
			//delay = get_tpcc_inter_tx_operation_think_time(event_content->applicative_content.tx_class_id);
			delay = pointer->configuration.inter_tx_operation_think_time;
			next_op = next_operation_request(event_content->applicative_content.tx_id, event_content->applicative_content.tx_run_number, delay, state, now);
			// check if the last operation has been executed...
			if (next_op == NULL) {
				//... yes, send the commit request to server
				commit_request(event_content->applicative_content.tx_id, event_content->applicative_content.tx_run_number, delay, now, state, event_content);
			} else {
				if (next_op->op_type == TX_GET)
					pointer->executed_get_operation++;
				if (next_op->op_type == TX_PUT)
					pointer->executed_put_operation++;
			}
			break;

		case TX_EXECUTION_EXCEPTION:
			if (pointer->configuration.client_verbose) {
				printf("C%i - ricevuto evento TX_EXECUTION_EXCEPTION con tx_id %i al tempo %f\n", pointer->client_id, event_content->applicative_content.tx_id, now);
			}
			pointer->active_transactions--;
			pointer->in_backoff_transactions++;
			pointer->aborted_runs++;
			tx_descr = get_transaction(event_content->applicative_content.tx_id, pointer->transaction_list);
			pointer->total_transaction_run_execution_time += now - tx_descr->last_run_start_time;
			//select back-off time
			delay = Expent(((double) pointer->configuration.backoff_time));
			tx_descr = get_transaction(event_content->applicative_content.tx_id, pointer->transaction_list);
			reset_transaction_state(tx_descr);

			pointer->tx_classes[tx_descr->tx_class_id]->aborted_tx_num += 1;
			pointer->tx_classes[tx_descr->tx_class_id]->tot_tx_num += 1;

			//rerun transaction
			tx_descr->last_run_start_time = now + delay;
			transaction_run_request(tx_descr, event_content->applicative_content.tx_run_number + 1, delay, state, now);
			break;

		case TX_COMMIT_RESPONSE:
			if (pointer->configuration.client_verbose) {
				printf("C%i - ricevuto evento TX_COMMIT_RESPONSE al tempo %f\n", pointer->client_id, now);
			}

			tx_descr = get_transaction(event_content->applicative_content.tx_id, pointer->transaction_list);

			pointer->total_transaction_execution_time += now - tx_descr->start_time;
			pointer->total_transaction_run_execution_time += now - tx_descr->last_run_start_time;
			pointer->committed_transactions++;
			pointer->active_transactions--;

			pointer->tx_classes[tx_descr->tx_class_id]->tot_response_time += now - tx_descr->start_time;
			pointer->tx_classes[tx_descr->tx_class_id]->committed_tx_num += 1;
			pointer->tx_classes[tx_descr->tx_class_id]->tot_tx_num += 1;

			//remove committed transaction from list
			remove_transaction(event_content->applicative_content.tx_id, &pointer->transaction_list);

			if (pointer->configuration.print_execution_info && pointer->client_id == 0) {
				if (pointer->committed_transactions % (int) ((double) pointer->configuration.number_of_transactions * 0.1) == 0) {
					printf("committed transactions: %i (%i%%)\n", pointer->committed_transactions,
							(int) (100 * (double) pointer->committed_transactions / (double) (pointer->configuration.number_of_transactions)));
				}
				fflush(stdout);
			}

			// there are further transaction to execute?...
			if (pointer->committed_transactions < pointer->configuration.number_of_transactions) {
				//..yes, send the next transaction request to server..
				if (pointer->configuration.system_model == CLOSED) {
					tx_descr = new_transaction(state, now);
					if (pointer->configuration.workload_type == 0) {
						delay = Expent(((double) pointer->configuration.inter_tx_think_time));
					} else {
						delay = tx_descr->previous_ntbc;
					}
					tx_descr->last_run_start_time = now + delay;
					tx_descr->start_time = now + delay;
					transaction_run_request(tx_descr, 0, delay, state, now);
				}
			} else {
				if (pointer->committed_transactions == pointer->configuration.number_of_transactions) {
					//...no, transactions terminated, print final statistics if requested...
					if (pointer->configuration.client_print_stat)
						print_final_statistics(state, now);
					if (pointer->ending == 0 && pointer->committed_transactions == pointer->configuration.number_of_transactions) {
						pointer->ending = 1;
					}
				}
			}
			break;
		}
		break;

	// used only with open system
	case RUN_NEW_TX:
		//send the next transaction request to server..
		tx_descr = new_transaction(state, now);
		tx_descr->start_time = now;
		tx_descr->last_run_start_time = now;
		transaction_run_request(tx_descr, 0, now, state, now);
		//schedule the next transaction request to server..
		delay = Expent(((double) 1 / pointer->configuration.tx_arrival_rate));
		ScheduleNewEvent(me, now + delay, RUN_NEW_TX, &new_event_content, sizeof(event_content_type));
		break;
	}
}
