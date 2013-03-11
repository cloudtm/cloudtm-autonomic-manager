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
#include <string.h>
#include <stdio.h>
#include "concurrency-control.h"
#include "concurrency-control-functions.h"
#include "hashing.h"
#include "../states.h"

static int active_transaction_table_hash_function(int tx_id) {
	return tx_id % (ACTIVE_TRANSACTION_TABLE_SIZE - 1);
}

transaction_metadata *get_transaction_metadata(int tx_id, SERVER_lp_state_type *pointer) {
	int hash_table_bucket = active_transaction_table_hash_function(tx_id);
	transaction_metadata * tmd = (transaction_metadata *) pointer->cc_metadata->active_transaction[hash_table_bucket];
	while (tmd != NULL) {
		if (tmd->tx_id == tx_id) {
			return tmd;
		}
		tmd = tmd->next;
	}
	return NULL;
}

int add_transaction_metadata(int tx_id, int local, int tx_run_number, SERVER_lp_state_type * pointer) {
	if (!get_transaction_metadata(tx_id, pointer)) {
		int hash_table_bucket = active_transaction_table_hash_function(tx_id);
		transaction_metadata * new_transaction_metadata = (transaction_metadata *) malloc(sizeof(transaction_metadata));
		new_transaction_metadata->tx_id = tx_id;
		new_transaction_metadata->executed_operations = 0;
		new_transaction_metadata->current_tx_run_number = tx_run_number;
		new_transaction_metadata->is_blocked = 0;
		new_transaction_metadata->expected_prepare_response_counter = 0;
		new_transaction_metadata->next = NULL;
		new_transaction_metadata->write_set = NULL;
		new_transaction_metadata->read_set = NULL;
		if (pointer->cc_metadata->active_transaction[hash_table_bucket] == NULL) {
			pointer->cc_metadata->active_transaction[hash_table_bucket] = new_transaction_metadata;
			return 1;
		} else {
			transaction_metadata * tmd = (transaction_metadata *) pointer->cc_metadata->active_transaction[hash_table_bucket];
			while (tmd->next != NULL) {
				if (tmd->tx_id == tx_id) {
					return -1;
				}
				tmd = tmd->next;
			}
			tmd->next = new_transaction_metadata;
			return 1;
		}
	}
	return 1;
}

int remove_transaction_metadata(int tx_id, SERVER_lp_state_type * pointer) {
	int hash_table_bucket = active_transaction_table_hash_function(tx_id);
	transaction_metadata * prev = NULL;
	transaction_metadata * tmd = (transaction_metadata *) pointer->cc_metadata->active_transaction[hash_table_bucket];
	while (tmd != NULL) {
		if (tmd->tx_id == tx_id) {
			if (prev == NULL) {
				pointer->cc_metadata->active_transaction[hash_table_bucket] = tmd->next;
			} else {
				prev->next = tmd->next;
			}
			free(tmd);
			return 1;
		}
		prev = tmd;
		tmd = tmd->next;
	}
	printf("ERRORE (remove_transaction_from_table):  Nessun active transaction con id %d presente\n", tx_id);
	return -1;

}

static int add_data_to_write_set(int tx_id, int client_id, SERVER_lp_state_type * pointer, int object_key_id) {
	data_set_entry * entry = (data_set_entry *) malloc(sizeof(data_set_entry));
	entry->object_key_id = object_key_id;
	entry->next = NULL;
	transaction_metadata * transaction = get_transaction_metadata(tx_id, pointer);
	if (transaction == NULL) {
		printf("ERROR: no transaction found with id %d (from client id %d)\n", tx_id, client_id);
		exit(-1);
	}
	if (transaction->write_set == NULL) {
		transaction->write_set = entry;
		return 1;
	}
	data_set_entry * current_entry = transaction->write_set;
	while (current_entry->next != NULL) {
		if (current_entry->object_key_id == object_key_id) {
			return 0;
		}
		current_entry = current_entry->next;
	}

	current_entry->next = entry;

	return 1;
}

static int add_data_to_read_set(int tx_id, int client_id, SERVER_lp_state_type * pointer, int object_key_id) {
	data_set_entry * entry = (data_set_entry *) malloc(sizeof(data_set_entry));
	entry->object_key_id = object_key_id;
	entry->next = NULL;
	transaction_metadata * transaction = get_transaction_metadata(tx_id, pointer);
	if (transaction == NULL) {
		printf("ERROR: no transaction found with id %d (from client id %d)\n", tx_id, client_id);
		exit(-1);
	}
	if (transaction->read_set == NULL) {
		transaction->read_set = entry;
		return 1;
	}
	data_set_entry * current_entry = transaction->read_set;
	while (current_entry->next != NULL) {
		if (current_entry->object_key_id == object_key_id) {
			return 0;
		}
		current_entry = current_entry->next;
	}
	current_entry->next = entry;
	return 1;
}

int remove_event_of_tx(CC_event_list * pointer, int txn_id) {
	if (pointer == NULL) {
		printf("ERROR: event list has not been correctly initialized\n");
		exit(-1);
	}

	if (pointer->event == NULL) {
		return 0;
	}

	CC_event_list *prev = NULL;
	CC_event_list *aux = pointer;

	while (aux != NULL) {
		if (aux->event == NULL) {
			printf("ERROR: an event in the list has not been correctly added\n");
			return -1;
		}
		if (aux->event->applicative_content.tx_id == txn_id) {
			if (prev == NULL && pointer->next != NULL) {
				aux = pointer->next;
				pointer->event = aux->event;
				pointer->next = aux->next;
				free(aux);

				prev = NULL;
				aux = pointer;
			} else if (prev == NULL && pointer->next == NULL) {
				pointer->event = NULL;
				pointer->next = NULL;
				return 1;
			} else {
				prev->next = aux->next;
				free(aux);

				aux = prev->next;
			}
		} else {
			prev = aux;
			aux = aux->next;
		}
	}
	return 1;
}

static event_content_type *get_next_event_waiting_for_object_key(state_type *state, int FLAG, int key) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	if (pointer->cc_metadata->event_queue == NULL) {
		printf("ERROR: event list has not been correctly initialized\n");
		exit(-1);
	}
	CC_event_list *aux;
	event_content_type *event_content;

	if (FLAG == CC_QUEUE) {
		aux = pointer->cc_metadata->event_queue;
	} else if (FLAG == CC_QUEUE_L1) {
		aux = pointer->cc_metadata->event_queue_L1;
	} else {
		printf("ERROR: flag %d is not valid\n", FLAG);
		return 0;
	}

	if (key < 0 || key > state->cache_objects) {
		printf("Error: key %i does not exists", key);
		exit(-1);
	}

	CC_event_list *prev = NULL;

	while (aux != NULL) {
		if (aux->event == NULL) {
			if (pointer->configuration.cc_verbose)
				printf("cc%d - No events exist in the waiting event list for object %i\n", pointer->server_id, key);
			return NULL;
		}
		if (aux->event->applicative_content.object_key_id == key) {

			event_content = aux->event;
			if (pointer->configuration.cc_verbose)
				printf("cc%d - Event found in the waiting event list for object %i \n", pointer->server_id, key);

			if (prev == NULL && aux->next != NULL) {
				if (FLAG == CC_QUEUE) {
					aux = pointer->cc_metadata->event_queue->next;
					pointer->cc_metadata->event_queue->event = aux->event;
					pointer->cc_metadata->event_queue->next = aux->next;
				} else { //FLAG == CC_QUEUE_L1
					aux = pointer->cc_metadata->event_queue_L1->next;
					pointer->cc_metadata->event_queue_L1->event = aux->event;
					pointer->cc_metadata->event_queue_L1->next = aux->next;
				}
			} else if (prev == NULL && aux->next == NULL) {
				if (FLAG == CC_QUEUE) {
					pointer->cc_metadata->event_queue->event = NULL;
					pointer->cc_metadata->event_queue->next = NULL;
				} else { //FLAG == CC_QUEUE_L1
					pointer->cc_metadata->event_queue_L1->event = NULL;
					pointer->cc_metadata->event_queue_L1->next = NULL;
				}
			} else {
				prev->next = aux->next;
			}

			if (pointer->configuration.cc_verbose)
				printf("cc%d - event of transaction %d, op_type %d, object %d removed from event list\n", pointer->server_id, event_content->applicative_content.tx_id,
						event_content->applicative_content.op_type, event_content->applicative_content.object_key_id);
			return event_content;
		}
		prev = aux;
		aux = aux->next;
	}

	if (pointer->configuration.cc_verbose)
		printf("cc%d - No events exist in the waiting event list for object %i\n", pointer->server_id, key);
	return NULL;
}

static void reschedule_event(state_type *state, double now, int object_key_id) {

	SERVER_lp_state_type *pointer = &state->type.server_state;
	event_content_type *event_content = NULL;

	do {
		event_content = get_next_event_waiting_for_object_key(state, CC_QUEUE, object_key_id);
		if (event_content != NULL) {
			transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			if (transaction != NULL)
				transaction->is_blocked = 0;
			ScheduleNewEvent(pointer->server_id, now, DELIVER_MESSAGE, event_content, sizeof(event_content_type));
			if (pointer->configuration.cc_verbose)
				printf("cc%d - event of transaction %d, op_type %d, object %d rescheduled at time %f\n", event_content->applicative_content.server_id, event_content->applicative_content.tx_id,
						event_content->applicative_content.op_type, event_content->applicative_content.op_number, now);
			remove_event_of_tx(pointer->cc_metadata->event_queue, event_content->applicative_content.tx_id);
		}
	} while (event_content != NULL);
}

static void remove_tx_locks(int tx_id, data_set_entry *data_set, state_type *state, double now) {

	SERVER_lp_state_type *pointer = &state->type.server_state;
	data_set_entry *entry = data_set;
	while (entry != NULL) {
		int need_to_unlock = 0;
		if (pointer->configuration.concurrency_control_type == ETL_2PL || pointer->configuration.concurrency_control_type == CTL_2PL)
			need_to_unlock = is_owner(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients, state->object_replication_degree);
		else if (pointer->configuration.concurrency_control_type == PRIMARY_OWNER_CTL_2PL)
			need_to_unlock = is_primary(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients);
		if (need_to_unlock) {
			if ((pointer->cc_metadata->locks[entry->object_key_id] != tx_id)) {
				if (pointer->configuration.cc_verbose)
					printf("cc%d - lock for object %i of transaction %i not found\n", pointer->server_id, entry->object_key_id, tx_id);
			} else {
				pointer->cc_metadata->locks[entry->object_key_id] = -1;
				if (pointer->configuration.cc_verbose)
					printf("cc%d - lock for object %i of transaction %i unlocked at time %f \n", pointer->server_id, entry->object_key_id, tx_id, now);
				//Wake up waiting event for key 'object_key_id';
				reschedule_event(state, now, entry->object_key_id);
			}
		}
		entry = entry->next;
	}
}

static void abort_local_tx(event_content_type * event_content, state_type *state, double now) {

	SERVER_lp_state_type *pointer = &state->type.server_state;
	//Remove all events of transaction
	remove_event_of_tx(pointer->cc_metadata->event_queue, event_content->applicative_content.tx_id);
	transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
	if (transaction == NULL) {
		printf("ERROR: no transaction found with id %i (from client %i)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
		exit(-1);
	}
	remove_tx_locks(transaction->tx_id, transaction->write_set, state, now);
}

static void abort_remote_tx(event_content_type * event_content, state_type *state, double now) {

	SERVER_lp_state_type *pointer = &state->type.server_state;
	//Remove all events of transaction
	remove_event_of_tx(pointer->cc_metadata->event_queue, event_content->applicative_content.tx_id);
	//Remove all locks of transaction
	remove_tx_locks(event_content->applicative_content.tx_id, event_content->applicative_content.write_set, state, now);
}

//concurrency control initialization
void concurrency_control_init(state_type *state) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	int i;
	cc_metadata *cc_met = (cc_metadata *) malloc(sizeof(cc_metadata));
	cc_met->locks = malloc(sizeof(int) * state->cache_objects);
	for (i = 0; i < ACTIVE_TRANSACTION_TABLE_SIZE; i++)
		cc_met->active_transaction[i] = NULL;

	cc_met->event_queue = (CC_event_list *) malloc(sizeof(CC_event_list));
	cc_met->event_queue->event = NULL;
	cc_met->event_queue->next = NULL;

	for (i = 0; i < state->cache_objects; i++) {
		cc_met->locks[i] = -1;
	}

	cc_met->lock_retry_num = 0;
	pointer->cc_metadata = cc_met;
}

//enqueue en event
int enqueue_event(SERVER_lp_state_type * pointer, event_content_type * next_event) {

	if (pointer->cc_metadata->event_queue == NULL) {
		printf("**ERRORE (enqueue_event): l'event_queue dovrebbe essere inizializzata\n");
		return 0;
	}

	CC_event_list *aux;
	aux = pointer->cc_metadata->event_queue;

	if (aux->event == NULL) {
		pointer->cc_metadata->event_queue->event = (event_content_type *) malloc(sizeof(event_content_type));
		memcpy(pointer->cc_metadata->event_queue->event, next_event, sizeof(event_content_type));
		if (pointer->configuration.cc_verbose)
			printf("cc%d - event of transaction %d , operation type %d, object %d added to waiting event list\n", pointer->server_id, next_event->applicative_content.tx_id,
					next_event->applicative_content.op_type, next_event->applicative_content.object_key_id);
		return 1;
	}

	CC_event_list *object = (CC_event_list *) malloc(sizeof(CC_event_list));
	object->event = (event_content_type *) malloc(sizeof(event_content_type));
	memcpy(object->event, next_event, sizeof(event_content_type));
	object->next = NULL;

	while (aux->next != NULL) {
		aux = aux->next;
	}
	aux->next = object;

	if (pointer->configuration.cc_verbose)
		printf("cc%d - event of transaction %d , operation type %d, object %d added to waiting event list\n", pointer->server_id, next_event->applicative_content.tx_id,
				next_event->applicative_content.op_type, next_event->applicative_content.object_key_id);

	return 1;
}

static int get_waiting_data_item(int locker, int FLAG, SERVER_lp_state_type * pointer) {
	CC_event_list *queue;
	if (FLAG == CC_QUEUE) {
		queue = pointer->cc_metadata->event_queue;
	} else if (FLAG == CC_QUEUE_L1) {
		queue = pointer->cc_metadata->event_queue_L1;
	} else {
		printf("ERROR: flag %d is not valid\n", FLAG);
		return -1;
	}
	if (queue == NULL) {
		printf("ERROR: queue pointer is null\n");
		return -1;
	}

	while (queue != NULL) {
		if (queue->event != NULL && queue->event->applicative_content.tx_id == locker && pointer->cc_metadata->locks[queue->event->applicative_content.object_key_id] != locker) {
			return queue->event->applicative_content.object_key_id;
		}
		queue = queue->next;
	}

	return -1;
}

//check waiting cycles
static int check_cycle(int locker, int tx_id, SERVER_lp_state_type * pointer) {
	int object_key_id = get_waiting_data_item(locker, CC_QUEUE, pointer);

	if (object_key_id != -1) { // check if the locker is waiting for another transaction
		// get the transaction which, in turn, is locking the object
		locker = pointer->cc_metadata->locks[object_key_id];
		if (locker == tx_id)
			return 1;
		else
			return 0;
	}
	return 0;
}

static int check_deadlock(event_content_type * event_content, SERVER_lp_state_type * pointer) {
	if (pointer == NULL || event_content == NULL)
		return -1;
	return check_cycle(pointer->cc_metadata->locks[event_content->applicative_content.object_key_id], event_content->applicative_content.tx_id, pointer);
}

int acquire_a_local_lock(state_type *state, time_type timeout, int timeout_event_type, event_content_type *event_content, double now) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	if (pointer->configuration.cc_verbose)
		printf("cc%d - object %i of transaction %i to be locked\n", pointer->server_id, event_content->applicative_content.object_key_id, event_content->applicative_content.tx_id);
	//check lock...
	if (pointer->cc_metadata->locks[event_content->applicative_content.object_key_id] == -1) {
		//not locked
		pointer->cc_metadata->lock_retry_num = 0;
		//acquire lock
		pointer->cc_metadata->locks[event_content->applicative_content.object_key_id] = event_content->applicative_content.tx_id;
		if (pointer->configuration.cc_verbose)
			printf("cc%d - object %i of transaction %i locked at time %f \n", pointer->server_id, event_content->applicative_content.object_key_id, event_content->applicative_content.tx_id, now);
		return 1;
	} else if (pointer->cc_metadata->locks[event_content->applicative_content.object_key_id] == event_content->applicative_content.tx_id) {
		// already locked by me
		return 1;
	} else {
		//already locked by another transaction
		pointer->cc_metadata->lock_retry_num++;
		//check deadlocks (if enabled)
		if (pointer->configuration.deadlock_detection_enabled && check_deadlock(event_content, pointer)) {
			return -1;
		}

		//add the timeout event
		event_content_type new_event_content;
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		ScheduleNewEvent(pointer->server_id, now + timeout, timeout_event_type, &new_event_content, sizeof(event_content_type));

		//enqueue event
		memcpy(&new_event_content, event_content, sizeof(event_content_type));
		new_event_content.applicative_content.object_key_id = event_content->applicative_content.object_key_id;
		enqueue_event(pointer, &new_event_content);

		transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction == NULL) {
			if (pointer->configuration.cc_verbose) {
				printf("cc%d - transaction %i is not local\n", pointer->server_id, event_content->applicative_content.tx_id);
				printf("cc%d - prepare of tx %i added in the waiting event queue %f due to a lock on object :%d tx:%i\n", pointer->server_id, event_content->applicative_content.tx_id, now,
						event_content->applicative_content.object_key_id, new_event_content.applicative_content.tx_id);
			}
			return 0;
		} else {
			transaction->is_blocked = 1;
			if (pointer->configuration.cc_verbose)
				printf("cc%d - tx %i is waiting at time %f due to a lock lock on object :%d tx:%i\n", pointer->server_id, event_content->applicative_content.tx_id, now,
						event_content->applicative_content.object_key_id, new_event_content.applicative_content.tx_id);
			return 0;
		}
	}
}

int acquire_local_locks(state_type *state, data_set_entry *data_set, time_type timeout, int timeout_event_type, event_content_type *event_content, double now) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	data_set_entry *entry = data_set;
	if (entry == NULL) {
		if (pointer->configuration.cc_verbose)
			printf("cc%d -  write set of transaction %d is empty\n", pointer->server_id, event_content->applicative_content.tx_id);
		return 1;
	}
	while (entry != NULL) {
		int need_to_lock = 0;
		if (pointer->configuration.concurrency_control_type == ETL_2PL || pointer->configuration.concurrency_control_type == CTL_2PL)
			need_to_lock = is_owner(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients, state->object_replication_degree);
		else if (pointer->configuration.concurrency_control_type == PRIMARY_OWNER_CTL_2PL)
			need_to_lock = is_primary(entry->object_key_id, pointer->server_id, state->num_servers, state->num_clients);
		if (need_to_lock) {
			if (pointer->configuration.cc_verbose)
				printf("cc%d - object %d for transaction %i to be locked\n", pointer->server_id, entry->object_key_id, event_content->applicative_content.tx_id);
			//check lock...
			if (pointer->cc_metadata->locks[entry->object_key_id] == -1) {
				//not locked
				pointer->cc_metadata->lock_retry_num = 0;
				//acquire lock
				pointer->cc_metadata->locks[entry->object_key_id] = event_content->applicative_content.tx_id;
				if (pointer->configuration.cc_verbose)
					printf("cc%d - object %d  for transaction  %i locked at time %f \n", pointer->server_id, entry->object_key_id, event_content->applicative_content.tx_id, now);
			} else if (pointer->cc_metadata->locks[entry->object_key_id] == event_content->applicative_content.tx_id) {
				// already locked by me
				// go to the next entry
			} else {
				//already locked by another transaction
				pointer->cc_metadata->lock_retry_num++;
				//check deadlock (if enabled)
				if (pointer->configuration.deadlock_detection_enabled && check_deadlock(event_content, pointer)) {
					return -1;
				}
				//add the timeout event
				event_content_type new_event_content;
				memcpy(&new_event_content, event_content, sizeof(event_content_type));
				ScheduleNewEvent(pointer->server_id, now + timeout, timeout_event_type, &new_event_content, sizeof(event_content_type));

				//enqueue transaction
				memcpy(&new_event_content, event_content, sizeof(event_content_type));
				new_event_content.applicative_content.object_key_id = entry->object_key_id;
				enqueue_event(pointer, &new_event_content);

				transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
				if (transaction == NULL) {
					if (pointer->configuration.cc_verbose) {
						printf("cc%d - transaction %i is not local\n", pointer->server_id, event_content->applicative_content.tx_id);
						printf("cc%d - prepare of tx %i added in the waiting event queue %f due to a lock on object %d tx:%i\n", pointer->server_id, event_content->applicative_content.tx_id, now,
								entry->object_key_id, new_event_content.applicative_content.tx_id);
					}
					return 0;
				} else {
					transaction->is_blocked = 1;
					if (pointer->configuration.cc_verbose)
						printf("cc%d - transaction %i is waiting at time %f due to a lock on object%d tx:%i\n", pointer->server_id, event_content->applicative_content.tx_id, now, entry->object_key_id,
								new_event_content.applicative_content.tx_id);
					return 0;
				}
			}
		}
		entry = entry->next;
	}
	return 1;
}

//main concurrency control function: return 0 if the event is enqueued, 1 if the accessing object is locked, -1 if transaction must be aborted
int CC_control(event_content_type * event_content, state_type *state, time_type now) {
	SERVER_lp_state_type *pointer = &state->type.server_state;
	if (pointer == NULL || event_content == NULL)
		return -1;
	switch (event_content->applicative_content.op_type) {

	case TX_BEGIN:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_BEGIN\n", pointer->server_id);
		return 1;
		break;

	case TX_GET:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_GET per tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);

		int return_from_add_data_to_read_set = add_data_to_read_set(event_content->applicative_content.tx_id, event_content->applicative_content.client_id, pointer,
				event_content->applicative_content.object_key_id);
		if (return_from_add_data_to_read_set == -1) {
			abort_local_tx(event_content, state, now);
			if (pointer->configuration.cc_verbose)
				printf("\tcc%d - ERROR while adding object %i in the read-set of transaction %d from client %d\n", pointer->server_id, event_content->applicative_content.object_key_id,
						event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			return -1;
		} else {
			if (return_from_add_data_to_read_set == 0 && pointer->configuration.cc_verbose)
				printf("\tcc%d - object %d is already in the read-set of transaction %d from client %d\n", pointer->server_id, event_content->applicative_content.object_key_id,
						event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			else if (pointer->configuration.cc_verbose)
				printf("\tcc%d - object %d added to the read-set of transaction tx %d\n", pointer->server_id, event_content->applicative_content.object_key_id,
						event_content->applicative_content.tx_id);
		}
		return 1;
		break;

	case TX_PUT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d -  TX_PUT per tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		//add data to write-set
		add_data_to_write_set(event_content->applicative_content.tx_id, event_content->applicative_content.client_id, pointer, event_content->applicative_content.object_key_id);
		if (pointer->configuration.concurrency_control_type == ETL_2PL
				&& is_owner(event_content->applicative_content.object_key_id, pointer->server_id, state->num_servers, state->num_clients, state->object_replication_degree)) {
			return acquire_a_local_lock(state, pointer->configuration.locking_timeout, TX_LOCAL_TIMEOUT, event_content, now);
		}
		return 1;
		break;

	case TX_PREPARE:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_PREPARE per tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		data_set_entry *dataset = event_content->applicative_content.write_set;
		int result = acquire_local_locks(state, dataset, pointer->configuration.locking_timeout, TX_PREPARE_TIMEOUT, event_content, now);
		if (result == -1)
			abort_local_tx(event_content, state, now);
		return result;
		break;

	case TX_COMMIT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_COMMIT per tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		if (pointer->configuration.concurrency_control_type == CTL_2PL || pointer->configuration.concurrency_control_type == PRIMARY_OWNER_CTL_2PL) {
			transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
			if (transaction == NULL) {
				printf("ERROR: no transaction found with id %d (from client id %d)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
				exit(-1);
			}
			data_set_entry *dataset = transaction->write_set;
			int result = acquire_local_locks(state, dataset, pointer->configuration.locking_timeout, TX_LOCAL_TIMEOUT, event_content, now);
			if (result == -1)
				abort_local_tx(event_content, state, now);
			return result;
		} else
			return 1;
		break;

	case TX_FINAL_LOCAL_COMMIT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_FINAL_LOCAL_COMMIT per tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		transaction_metadata *transaction = get_transaction_metadata(event_content->applicative_content.tx_id, pointer);
		if (transaction == NULL) {
			printf("ERROR: no transaction found with id %d (from client id %d)\n", event_content->applicative_content.tx_id, event_content->applicative_content.client_id);
			exit(-1);
		}
		dataset = transaction->write_set;
		remove_tx_locks(event_content->applicative_content.tx_id, dataset, state, now);
		return 1;
		break;

	case TX_FINAL_REMOTE_COMMIT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_FINAL_REMOTE_COMMIT per tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		remove_tx_locks(event_content->applicative_content.tx_id, event_content->applicative_content.write_set, state, now);
		return 1;
		break;

	case TX_LOCAL_ABORT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_ABORT  per tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		abort_local_tx(event_content, state, now);
		return -1;
		break;

	case TX_REMOTE_ABORT:
		if (pointer->configuration.cc_verbose)
			printf("\tcc%d - TX_ABORT  per tx %i\n", pointer->server_id, event_content->applicative_content.tx_id);
		abort_remote_tx(event_content, state, now);
		return -1;
		break;

	default:
		printf("ERROR: operation type %i not managed\n", event_content->applicative_content.op_type);
		exit(-1);
	}
}

int has_locked(event_content_type * event_content, SERVER_lp_state_type * pointer) {
	return pointer->cc_metadata->locks[event_content->applicative_content.object_key_id] == event_content->applicative_content.tx_id;
}
