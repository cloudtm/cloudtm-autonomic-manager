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
#ifndef _DESCRIPTORS_H
#define _DESCRIPTORS_H

//operation descriptor pointer
typedef struct _operation_descriptor *operation_descriptor_pointer;

//operation descriptor
typedef struct _operation_descriptor {
	int op_number; // operation number
	int op_type; // operation type
	int object_key_id; // object id to be accessed
	double previous_tcb; // think time before this operation
	operation_descriptor_pointer next; //pointer to next operation descriptor
} operation_descriptor;

//transaction operation descriptor
typedef struct _transaction_descriptor *transaction_descriptor_pointer;

//operation descriptor
typedef struct _transaction_descriptor {
	int tx_id; // transaction id
	int tx_class_id; // transaction class id
	int server_id;
	double start_time; // time when the first transaction run is started
	double last_run_start_time; // 	time when the last transaction run is started
	double previous_ntbc; // think time before this transaction
	double last_tbc; // think time before the commit request (if needed)
	operation_descriptor *first_operation; // pointer to first operation of transaction
	operation_descriptor *last_operation; // pointer to last operation of transaction
	operation_descriptor *current_operation; // pointer to current operation of transaction to be executed
	transaction_descriptor_pointer next; // pointer to next transaction descriptor
} transaction_descriptor;

#endif /* _DESCRIPTORS_H */
