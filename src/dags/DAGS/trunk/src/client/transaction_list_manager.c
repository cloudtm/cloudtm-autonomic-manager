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

#include <string.h>
#include <time.h>
#include <stdlib.h>
#include <stdio.h>
#include "../states.h"
#include "descriptors.h"

/****** STATIC ******/

// returns an operation accessing object_key_id, if any
static operation_descriptor *get_operation(transaction_descriptor * tx_desc, int object_key_id) {

	if (tx_desc->first_operation == NULL) {
		return NULL;
	}

	if (tx_desc->first_operation->object_key_id == object_key_id) {
		return tx_desc->first_operation;
	}

	operation_descriptor *op_descr = tx_desc->first_operation;

	while (op_descr->next) {

		if (op_descr->next->object_key_id == object_key_id) {
			return op_descr->next;
		}

		op_descr = op_descr->next;
	}

	return NULL;
}

// inserisce una transazione alla fine della lista
static void add_transaction(transaction_descriptor ** tx_list, transaction_descriptor * tx_descr) {

	// scorre la lista fino alla prima tx con next nullo
	if ((*tx_list) == NULL) {

		*tx_list = tx_descr;

	} else {

		transaction_descriptor_pointer tx_list_temp = *tx_list;

		while (tx_list_temp->next) {
			tx_list_temp = tx_list_temp->next;
		}

		tx_list_temp->next = tx_descr;
	}
}

// inserisce un'operazione alla fine della transazione
static void enqueue_operation(transaction_descriptor * transaction, operation_descriptor * operation) {

	// ricalcola i puntatori
	if (transaction->last_operation != NULL) {
		transaction->last_operation->next = operation;
	}

	transaction->last_operation = operation;

	if (transaction->first_operation == NULL) {
		transaction->first_operation = operation;
		transaction->current_operation = operation;
	}
}

//rimuove tutte le operazioni da una transazione
static void remove_all_operations(transaction_descriptor * tx_desc) {

	operation_descriptor *to_delete;

	if (tx_desc->first_operation == NULL) {

		return;

	} else {

		while (tx_desc->first_operation != NULL) {
			to_delete = tx_desc->first_operation;
			tx_desc->first_operation = tx_desc->first_operation->next;
			// libera la memoria dell'operazione
			free(to_delete);
		}
		return;
	}
}

/****** NON-STATIC ******/

// restituisce l'operazione corrente della transazione e passa alla successiva
operation_descriptor *get_next_operation(transaction_descriptor * tx_desc) {

	if (tx_desc != NULL && tx_desc->current_operation != NULL) {

		operation_descriptor *op_descr = tx_desc->current_operation;
		tx_desc->current_operation = tx_desc->current_operation->next;

		return op_descr;

	} else {

		return NULL;
	}
}

// resetta i puntatori current della transazione
void reset_transaction_state(transaction_descriptor * transaction) {

	// resetta il current della transazione
	if (transaction->first_operation != NULL) {
		transaction->current_operation = transaction->first_operation;
	}
}

int select_warehouse() { //1 warehouse
	return 0;
}

int select_district() { //10 stocks
	return RandomRange(1, 10);
}

int select_stock() { //100000 strocks
	return RandomRangeNonUniform(8191, 11, 100010);
}

int select_item() { //100000 item
	return RandomRangeNonUniform(8191, 100011, 200010);
}

int select_customer(int district) { //3000 customer
	return RandomRangeNonUniform(1023, 200011, 203010) + (district - 1) * 3000;
}

int from_item_to_stock(int item) {
	return item - 100011 + 11;
}

//230011

enum tx_class {
	NEW_ORDER, PAYMENT,
};

double get_tpcc_inter_tx_operation_think_time(int tx_class_id) {
	if (tx_class_id == NEW_ORDER) {
		return Expent(0.0); //0.035
	} else if (tx_class_id == PAYMENT) {
		return Expent(0.0);
	} else {
		printf("\nErrore: classe transazionale non corretta");
		return 0;
	}
}

// funzione per creare una transazione ed accodarla
transaction_descriptor *create_new_tpcc_transaction(state_type *state, transaction_descriptor ** tx_list, int tx_id) {

	// Get the client configuration from simulation state
	CLIENT_lp_state_type *pointer = &state->type.client_state;

	operation_descriptor *op = NULL;
	int i = 0;
	int op_number = 1;
	int tx_class_id = -1;
	transaction_descriptor *tx_descr = (transaction_descriptor *) malloc(sizeof(transaction_descriptor));

	tx_descr->tx_id = tx_id;
	tx_descr->next = NULL;
	tx_descr->first_operation = NULL;
	tx_descr->last_operation = NULL;
	tx_descr->current_operation = NULL;

	if (pointer->configuration.tlm_verbose) {
		printf("creazione nuova transazione tx_id %i\n", tx_id);
	}

	//crea le operazioni
	// seleziona la classe transazionale
	double rand_number = Random();
	double sum = 0;

	for (i = 0; i < pointer->configuration.number_of_tx_classes; i++) {
		sum += pointer->configuration.tx_class_probability[i];
		if (rand_number < sum) {
			tx_class_id = i;
			break;
		}
	}

	tx_descr->tx_class_id = tx_class_id;

	if (tx_class_id == NEW_ORDER) {
		/*
		 * NEW ORDER TRANSACTION
		 */
		int warehouse = select_warehouse();
		int district = select_district();
		int customer = select_customer(district);

		//crea l'operazione customer
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_GET;
		op->object_key_id = customer;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_GET customer\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		//crea l'operazione warehouse
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_GET;
		op->object_key_id = warehouse;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_GET warehouse\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		//crea l'operazione district
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_GET;
		op->object_key_id = district;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_GET district\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		int i;
		int top = RandomRange(5, 15);
		for (i = 1; i <= top; i++) {

			//crea l'operazione item
			op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
			op->op_number = op_number++;
			op->op_type = TX_GET;
			int item = select_item();
			op->object_key_id = item;

			op->next = NULL;
			enqueue_operation(tx_descr, op);
			if (pointer->configuration.tlm_verbose) {
				printf("\t\ttipo TX_GET item\n");
				printf("\t\tdata_item %i\n", op->object_key_id);
			}

			//crea l'operazione stock
			op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
			op->op_number = op_number++;
			op->op_type = TX_GET;
			int stock = from_item_to_stock(item);
			op->object_key_id = stock;

			op->next = NULL;
			enqueue_operation(tx_descr, op);
			if (pointer->configuration.tlm_verbose) {
				printf("\t\ttipo TX_GET stock\n");
				printf("\t\tdata_item %i\n", op->object_key_id);
			}

			//crea l'operazione stock
			op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
			op->op_number = op_number++;
			op->op_type = TX_PUT;
			op->object_key_id = stock;

			op->next = NULL;
			enqueue_operation(tx_descr, op);
			if (pointer->configuration.tlm_verbose) {
				printf("\t\ttipo TX_PUT stock\n");
				printf("\t\tdata_item %i\n", op->object_key_id);
			}
		}

	} else if (tx_class_id == PAYMENT) {

		/*
		 * PAYMENT TRANSACTION
		 */

		int warehouse = select_warehouse();
		int district = select_district();
		int customer = select_customer(district);

		//crea l'operazione warehouse
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_GET;
		op->object_key_id = warehouse;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_GET warehouse\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		//crea l'operazione warehouse
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_PUT;
		op->object_key_id = warehouse;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_PUT warehouse\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		//crea l'operazione district
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_GET;
		op->object_key_id = district;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_GET district\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		//crea l'operazione district
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_PUT;
		op->object_key_id = district;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_PUT district\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		//crea l'operazione customer
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_GET;
		op->object_key_id = customer;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_GET customer\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		//crea l'operazione customer
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = op_number++;
		op->op_type = TX_PUT;
		op->object_key_id = customer;

		op->next = NULL;
		enqueue_operation(tx_descr, op);
		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo TX_PUT customer\n");
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

	} else {
		printf("\nErrore: classe transazionale non corretta");
	}

	//resetta lo stato della transazione
	reset_transaction_state(tx_descr);

	//accoda la transazione
	add_transaction(tx_list, tx_descr);

	return tx_descr;
}

// funzione per creare una transazione ed accodarla
transaction_descriptor *create_new_synthetic_transaction(state_type *state, transaction_descriptor ** tx_list, int tx_id, double data_items_zipf_const) {

	// Get the client configuration from simulation state
	CLIENT_lp_state_type *pointer = &state->type.client_state;
	operation_descriptor *op = NULL;
	int number_of_operations = 0;
	int i, j;
	int tx_class_id = -1;
	double val;
	transaction_descriptor *tx_descr = (transaction_descriptor *) malloc(sizeof(transaction_descriptor));

	tx_descr->tx_id = tx_id;
	tx_descr->next = NULL;
	tx_descr->first_operation = NULL;
	tx_descr->last_operation = NULL;
	tx_descr->current_operation = NULL;

	if (pointer->configuration.tlm_verbose) {
		printf("creazione nuova transazione tx_id %i\n", tx_id);
	}

	//crea le operazioni
	// seleziona la classe transazionale
	double rand_number = Random();
	float sum = 0;

	for (i = 0; i < pointer->configuration.number_of_tx_classes; i++) {
		sum += pointer->configuration.tx_class_probability[i];
		if (rand_number < sum) {
			tx_class_id = i;
			break;
		}
	}

	tx_descr->tx_class_id = tx_class_id;

	// seleziona la lunghezza delle transazioni in operazioni
	switch (pointer->configuration.transaction_length_type) {
	case FIXED:
		number_of_operations = pointer->configuration.tx_class_length[tx_class_id];
		//number_of_operations=5; //TODO rimettere la lungezza delle transazioni in modo corretto
		break;
	case UNIFORM:
		//number_of_operations=5;
		number_of_operations = RandomRange(0, pointer->configuration.tx_class_length[tx_class_id] - 1) + 1;
		break;
	case ZIPF:
	case SINGLE_BAR:
	default:
		fprintf(stderr, "Unsupported value %d at %s:%d\n", pointer->configuration.transaction_length_type, __FILE__, __LINE__);
	}

	for (j = 1; j <= number_of_operations; j++) {
		if (pointer->configuration.tlm_verbose) {
			printf("\tcreo operazione %i\n", j);
		}

		//crea l'operazione
		op = (operation_descriptor *) malloc(sizeof(operation_descriptor));
		op->op_number = j;
		// seleziona il tipo di accesso
		double r;
		do {
			r = Random();
		} while (r == 1);

		if (r <= pointer->configuration.tx_class_write_probability[tx_class_id]) {
			op->op_type = TX_PUT;
		} else {
			op->op_type = TX_GET;
		}

		operation_descriptor *l;

		//selezione distribuzione accesso ai data items
		//TODO

		switch (pointer->configuration.object_access_distribution_type[tx_class_id]) {
		//int dist=UNIFORM;
		//switch (dist) {

		case UNIFORM:

			// seleziona #pointer->cache_objects diversi
			do {
				op->object_key_id = RandomRange(0, state->cache_objects - 1);
				l = get_operation(tx_descr, op->object_key_id);
			} while (l);

			break;

		case TOPKEYS:

			rand_number = Random();
			//printf("Random: %f\n", rand_number);
			float * objects_access_probability;
			op->object_key_id = -1;
			int id_item;
			int number_of_top_keys = 0;
			long double no_tk_probability;
			long double sum_tk = 0;
			bool top_key = false;

			//printf("%d - %d\n",op->op_type, TX_PUT);
			if (op->op_type == TX_GET) {
				objects_access_probability = pointer->configuration.topkeys_cache_objects_get_probability;
				number_of_top_keys = pointer->configuration.number_of_gets_topkeys;
			} else {
				objects_access_probability = pointer->configuration.topkeys_cache_objects_put_probability;
				number_of_top_keys = pointer->configuration.number_of_puts_topkeys;
			}

			for (id_item = 0; id_item < number_of_top_keys; id_item++) {
				sum_tk += objects_access_probability[id_item];
				//printf("sum: %f\n",sum);

				if (rand_number < sum_tk) {
					top_key = true;
					op->object_key_id = id_item;
					break;
				}
			}

			if (!top_key) {

				no_tk_probability = (1 - sum_tk) / (state->cache_objects - number_of_top_keys);

				for (; id_item < state->cache_objects; id_item++) {
					sum_tk += no_tk_probability;
					//printf("sum-no_tk: %f\n",sum);

					if (rand_number < sum_tk) {
						op->object_key_id = id_item;
						break;
					}
				}
			}

			break;

			//printf("Scelto: %d\n",op->object_key_id);

		case ZIPF:

			op->object_key_id = Zipf(data_items_zipf_const, state->cache_objects);

			break;

		case SINGLE_BAR:

			val = Random();

			if (val < 0.8) { // TODO: Alessandro: parametrizzare in una macro?!
				op->object_key_id = RandomRange(0, 10 - 1); //TODO va sul 20% dei data_item
			} else {
				op->object_key_id = RandomRange(0, state->cache_objects - 10 - 1) + 10; //restante 80%
			}

			break;
		}

		if (pointer->configuration.tlm_verbose) {
			printf("\t\ttipo %i\n", op->op_type);
			printf("\t\tdata_item %i\n", op->object_key_id);
		}

		op->next = NULL;
		enqueue_operation(tx_descr, op);
	}

	//resetta lo stato della transazione
	reset_transaction_state(tx_descr);

	//accoda la transazione
	add_transaction(tx_list, tx_descr);

	return tx_descr;
}

//restituisce la transazione tx_id
transaction_descriptor *get_transaction(int tx_id, transaction_descriptor * tx_list) {

	//scorre la lista delle transazioni fino a quando non trova l'id dela tx

	if (tx_list == NULL) {

		return NULL;

	} else {

		transaction_descriptor_pointer tx_temp = tx_list;

		while (tx_temp != NULL && tx_temp->tx_id != tx_id) {
			tx_temp = tx_temp->next;
		}

		if (tx_temp != NULL && tx_temp->tx_id == tx_id) {
			return tx_temp;
		} else {
			return NULL;
		}
	}
}

// rimuove una transazione dalla lista
void remove_transaction(int tx_id, transaction_descriptor ** tx_list) {

	// scorre la lista fino alla tx tx_id
	if ((*tx_list) == NULL) {

		return;

	} else {

		if ((*tx_list)->tx_id == tx_id) {
			transaction_descriptor *to_delete = *tx_list;
			*tx_list = (*tx_list)->next;

			// libera la memoria dalle operazioni,
			remove_all_operations(to_delete);

			// libera la memoria della transazione
			free(to_delete);

			return;

		} else {

			transaction_descriptor *tx_temp = *tx_list;

			while (tx_temp->next) {

				if (tx_temp->next->tx_id == tx_id) {

					transaction_descriptor *to_delete = tx_temp->next;
					tx_temp->next = tx_temp->next->next;

					// libera la memoria dalle operazioni,
					remove_all_operations(to_delete);

					// libera la memoria della transazione
					free(to_delete);

					return;
				}

				tx_temp = tx_temp->next;
			}

			return;
		}
	}
}

