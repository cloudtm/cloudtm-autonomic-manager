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
#ifndef _APPLICATION_H
#define _APPLICATION_H

#include "ROOT-Sim.h"
#include "transaction.h"


/* simulation objects */
enum LP_type {
	CLIENT,
	SERVER
};




/* Definition of event types */
enum events {
	TX_GET = 1,	// first event MUST be 1
	TX_PUT,
	TX_REMOTE_GET,
	TX_REMOTE_GET_RETURN,
	TX_BEGIN,
	TX_BEGIN_RETURN,
	TX_PUT_RETURN,
	TX_GET_RETURN,
	TX_COMMIT,
	TX_COMMIT_RESPONSE,
	TX_LOCAL_ABORT,
	TX_REMOTE_ABORT,
	TX_FINAL_LOCAL_COMMIT,
	TX_FINAL_REMOTE_COMMIT,
	//CLIENT->CLIENT
	RUN_STARTED,

	RUN_NEW_TX,
	//SERVER->SERVER
	TX_PREPARE,
	TX_PREPARE_SUCCEEDED,
	TX_PREPARE_FAILED,
	TX_FINAL_COMMIT,
	TX_DISTRIBUTED_FINAL_COMMIT,
	//SERVER->NET
	SEND_REQ_FIRST_OWNER,
	SEND_VALUE,
	//SERVER->NET(TO CLIENT)
	TX_EXECUTION_EXCEPTION,
	/****** EVENTI PER PUT *******/
	//Client->Net
	SEND_PUT_REQUES,
	//Net->Server
	DELIVER_PUT_REQUEST,
	DELIVER_OWNER_PUT,
	DELIVER_OWNER_PUT_LOCK,
	UPDATE,
	//Server->Server
	TX_LOCAL_TIMEOUT,
	TX_PREPARE_TIMEOUT,
	//Net->Client
	DELIVER_MESSAGE,
	/*********** CPU ****************/
	CPU_LOCAL_TX_GET,
	CPU_LOCAL_TX_PUT,
	CPU_REMOTE_TX_PUT,
	CPU_PREPARE,
	CPU_TX_BEGIN,
	CPU_TX_LOCAL_ABORT,
	CPU_TX_REMOTE_ABORT,
	CPU_PROCESSING_REQUEST_EXECUTED,
	CPU_PROCESSING_REQUEST,
	CPU_REMOTE_READ_EXECUTION,
	CPU_SEND_REMOTE_TX_GET,
	CPU_LOCAL_TX_GET_FROM_REMOTE,
	CPU_REMOTE_TX_GET_RETURN,
	CPU_UPDATE,
	CPU_LOCAL_PREPARE_SUCCESSED,
	CPU_LOCAL_PREPARE_FAILED,
	CPU_DISTRIBUTED_FINAL_TX_COMMIT,
	CPU_LOCAL_TX_FINAL_COMMIT,
	CPU_TX_PREPARE_FAILED,

	PRINT_STAT,  /*****MAX****/
	CC_QUEUE,
	CC_QUEUE_L1,
};


/***********************************************************************/


typedef struct _application_event_content_type {
	int client_id;
	int server_id;
	int owner_id;
	int tx_class_id;
	int tx_id;
	int tx_run_number;
	int op_number;
	int op_type;
	int object_key_id;
	double time;
	data_set_entry * write_set;
} application_event_content_type;


typedef struct _event_content_type {
	application_event_content_type applicative_content;
	int destination_object_id;
	int origin_object_id;
} event_content_type;

void ProcessEvent(unsigned int me, time_type now, int event_type, event_content_type *event_content, unsigned int size, void *pointer);

#endif /* _APPLICATION_H */
