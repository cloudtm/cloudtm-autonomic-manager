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
#ifndef _TRANSACTIONS_H
#define _TRANSACTIONS_H

#define ACTIVE_TRANSACTION_TABLE_SIZE	256

typedef struct _data_set_entry *data_set_entry_pointer;

typedef struct _data_set_entry {
	int object_key_id;
	data_set_entry_pointer next;
} data_set_entry;

typedef struct _transaction_metadata *transaction_metadata_pointer;

typedef struct _transaction_metadata {
	int tx_id;;
	int current_tx_run_number;
	int executed_operations;
	int is_blocked;
	int expected_prepare_response_counter; //counter for waiting prepare responses from other servers
	data_set_entry *write_set;
	data_set_entry *read_set;
	transaction_metadata_pointer next;
} transaction_metadata;

#endif /* _TRANSACTIONS_H */
