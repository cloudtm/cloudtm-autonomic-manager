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

//puntatore descrittore operazione
typedef struct _operation_descriptor *operation_descriptor_pointer;

//struttura descrittore operazione
typedef struct _operation_descriptor {
	int op_number;		// indica il numero progressivo dell'operazione nello statement 
	int op_type;		// indica il tipo di operazione
	int object_key_id;		// indica il dataitem all'interno della tabella
	double previous_tcb;	// indica il tbc precedente alla operazione
	operation_descriptor_pointer next;
} operation_descriptor;



//puntatore descrittore transazione
typedef struct _transaction_descriptor *transaction_descriptor_pointer;

//struttura descrittore transazione
typedef struct _transaction_descriptor {
	int tx_id;		// viene assegnato a run time dal TM
	int tx_class_id;	// indica la casse della tx
	int server_id;
	double start_time;	//indica il tempo in cui inizia
	double last_run_start_time;
	double previous_ntbc;	// indica il ntbc precedente alla operazione
	double last_tbc;	// indica l'ultimo tbc prima del commit
	operation_descriptor *first_operation;	// punta alla prima operazione della transazione
	operation_descriptor *last_operation;	// punta all'ultima operazione della transazione
	operation_descriptor *current_operation;	// punta all'operazione corrente in esecuzione di questo statements
	transaction_descriptor_pointer next;	// punta alla prossima transazione
} transaction_descriptor;

#endif /* _DESCRIPTORS_H */
