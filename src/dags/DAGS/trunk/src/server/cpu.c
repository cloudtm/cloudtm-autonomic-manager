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
#include "../states.h"
#include "../lib/events_wait_queue.h"

static double get_cpu_service_demand(SERVER_lp_state_type *pointer, event_content_type *event_content) {

	if (event_content->applicative_content.op_type == CPU_TX_LOCAL_GET)
		return pointer->configuration.local_tx_get_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_LOCAL_PUT)
		return pointer->configuration.local_tx_put_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_REMOTE_PUT)
		return pointer->configuration.remote_tx_put_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_BEGIN)
		return pointer->configuration.tx_begin_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_LOCAL_ABORT)
		return pointer->configuration.tx_abort_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_REMOTE_ABORT)
		return pointer->configuration.tx_abort_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_PREPARE)
		return pointer->configuration.tx_prepare_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_SEND_REMOTE_GET)
		return pointer->configuration.tx_send_remote_tx_get_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_LOCAL_GET_FROM_REMOTE)
		return pointer->configuration.local_tx_get_from_remote_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_REMOTE_GET_RETURN)
		return pointer->configuration.remote_tx_get_return_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_UPDATE)
		return pointer->configuration.update_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_LOCAL_PREPARE_SUCCESSED)
		return pointer->configuration.local_prepare_successed_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_LOCAL_PREPARE_FAILED)
		return pointer->configuration.local_prepare_failed_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_DISTRIBUTED_FINAL_COMMIT)
		return pointer->configuration.distributed_final_tx_commit_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_LOCAL_FINAL_COMMIT)
		return pointer->configuration.local_tx_final_commit_cpu_service_demand;
	else if (event_content->applicative_content.op_type == CPU_TX_PREPARE_FAILED)
		return pointer->configuration.tx_prepare_failed_cpu_service_demand;

	printf("cpu%d - ERRORE: cpu service demand non trovato per operazione tipo: %i\n", pointer->server_id, event_content->applicative_content.op_type);
	return -1.0;

}

static void execute_processing_request(SERVER_lp_state_type *pointer, time_type now, event_content_type *event_content) {
	double cpu_service_time = Expent(get_cpu_service_demand(pointer, event_content));
	ScheduleNewEvent(pointer->server_id, now + cpu_service_time, CPU_PROCESSING_REQUEST_EXECUTED, event_content, sizeof(event_content_type));
	ScheduleNewEvent(pointer->server_id, now + cpu_service_time, event_content->applicative_content.op_type, event_content, sizeof(event_content_type));
	if (pointer->configuration.server_verbose) {
		("cpu%d - event CPU_PROCESSING_REQUEST_EXECUTED sent to server at time %f\n", pointer->server_id, now + cpu_service_time);
	}
}

void add_processing_request(SERVER_lp_state_type* state, event_content_type *event_content, time_type now) {
	event_content_type new_event_content;
	memcpy(&new_event_content, event_content, sizeof(event_content_type));
	//if a servant is available...
	if (state->cpu_state.available_servents > 0) {
		state->cpu_state.available_servents--;
		// execute request
		execute_processing_request(state, now, &new_event_content);
	} else { // otherwise...
		copy_into_wait_queue(0, event_content, now, state->cpu_state.requests_queue);
	}
}

void cpu_init(SERVER_lp_state_type *pointer) {
	if (pointer->configuration.server_verbose) {
		printf("- Funzione Cpu_ProcessEvent: ricevuto evento INIT\n");
	}
	pointer->cpu_state.available_servents = pointer->configuration.max_servents_per_cpu;
	pointer->cpu_state.requests_queue = (event_wait_queue*) malloc(sizeof(event_wait_queue));
	pointer->cpu_state.requests_queue->bottom = NULL;
	pointer->cpu_state.requests_queue->top = NULL;
}

void cpu_request_executed(SERVER_lp_state_type *pointer, time_type now) {
	event_content_type * event_content;
	int event_type;
	get_from_wait_queue(&event_type, &event_content, pointer->cpu_state.requests_queue);
	if (event_content != NULL) {
		execute_processing_request(pointer, now, event_content);
		free(event_content);
	} else {
		pointer->cpu_state.available_servents++;
	}
}

