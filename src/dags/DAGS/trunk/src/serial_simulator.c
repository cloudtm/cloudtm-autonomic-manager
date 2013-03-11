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
#include <math.h>
#include <stdlib.h>
#include <stdio.h>
#include "application.h"
#include "configuration.h"
#include "ROOT-Sim.h"

#ifdef SERIAL_COMPILE
void numerical_init(void);
#endif

unsigned int n_prc_tot = NUM_OBJECTS;
unsigned int processed_event=0;
struct timeval start_time;
void *object_states[NUM_OBJECTS];
unsigned int cur_me;

//event pointer
typedef struct _event *event_pointer;

//event structure
typedef struct _event {
	unsigned int receiver; // object receiving the event
	time_type timestamp; //time when event must be processed
	int event_type; //event tipe
	event_content_type *event_content; //applicative content of the event
	event_pointer next; //nex event pointer
} event;

//first and last event pointer
event *first_event;
event *last_event;

//set state of an object
void SetState(void *pointer) {
	object_states[cur_me] = pointer;
}

//add event to list
void add_to_event_list(event *ev) {
	if (first_event == NULL) {
		first_event = ev;
		last_event = ev;
	} else if (ev->timestamp >= last_event->timestamp) {
		//add event at the end
		last_event->next = ev;
		last_event = ev;
	} else if (ev->timestamp < first_event->timestamp) {
		//add event on top
		ev->next = first_event;
		first_event = ev;
	} else {
		event_pointer event_temp = first_event;
		while (event_temp->next) {
			if (ev->timestamp < event_temp->next->timestamp) {
				//add event
				ev->next = event_temp->next;
				event_temp->next = ev;
				return;
			}
			event_temp = event_temp->next;
		}
		//add evend at the end
		event_temp->next = ev;
	}
	return;
}

//process the next event in the queye
int deliver_next_event() {
	if (first_event) {
		event_pointer event_to_deliver = first_event;
		first_event = event_to_deliver->next;
		ProcessEvent(event_to_deliver->receiver, event_to_deliver->timestamp, event_to_deliver->event_type, event_to_deliver->event_content, (unsigned int) sizeof(event_to_deliver->event_content),
				object_states[event_to_deliver->receiver]);
		processed_event++;
		// delete processed event
		free(event_to_deliver->event_content);
		free(event_to_deliver);
		return 1;
	}
	return 0;
}

// schedule a new event
void ScheduleNewEvent(unsigned int destination, time_type timestamp, unsigned int event_type, void *event_content, unsigned int size) {
	event *ev = (event*) malloc(sizeof(event));
	ev->receiver = destination;
	ev->event_type = event_type;
	ev->timestamp = timestamp;
	if (event_content != NULL) {
		ev->event_content = (event_content_type*) malloc(size);
		memcpy(ev->event_content, event_content, size);
	} else {
		ev->event_content = NULL;
	}
	ev->next = NULL;
	add_to_event_list(ev); // add event
}

double ProcessedEventsPerSecond() {
	struct timeval time_temp;
	gettimeofday(&time_temp, NULL);
	double processed=(double)processed_event/ (double)((time_temp.tv_sec+(double)time_temp.tv_usec/1000000)-(start_time.tv_sec+(double)start_time.tv_usec/1000000));
	processed_event=0;
	gettimeofday(&start_time, NULL);
	return processed;
}

int main(int argc, char* argv[]) {
	int i;
	char *parameters[8] = {"conf-file", "simulation.conf", "conf-file_1", "top_keys_puts.conf", "conf-file_2", "top_keys_gets.conf"};
	numerical_init();
	// send INIT event to all simulation objects
	for (i = 0; i < NUM_OBJECTS; i++) {
		cur_me = i;
		ProcessEvent(i, 0., 0, (void*) &parameters, 6, object_states[i]);
	}
	gettimeofday(&start_time, NULL);
	while (deliver_next_event());
	return 0;
}

