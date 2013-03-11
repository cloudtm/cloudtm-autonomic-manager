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

#include <stdio.h>
#include "events_wait_queue.h"

int get_queue_length(event_wait_queue *queue) {
	int i = 0;
	if (queue->top) {
		wait_event *ev = queue->top;
		while (ev) {
			ev = ev->next;
			i++;
		}
	}
	return i;
}

void copy_into_wait_queue(int event_type, event_content_type *event_content, time_type now, event_wait_queue *queue) {

	wait_event *event;
	event_content_type *new_event_content = (event_content_type*) malloc(sizeof(event_content_type));

	if (new_event_content == NULL) {
		printf("Copy_into_wait_queue: malloc failed\n");
	}

	memcpy(new_event_content, event_content, sizeof(event_content_type));

	event = (wait_event*) malloc(sizeof(wait_event));
	if (event == NULL) {
		printf("Copy_into_wait_queue: malloc failed\n");
	}

	event->event_type = event_type;
	event->event_content = new_event_content;
	event->insertion_time = now;
	event->next = NULL;

	if (queue->bottom != NULL) {
		queue->bottom->next = event;
	}

	queue->bottom = event;

	if (queue->top == NULL) {
		queue->top = event;
	}
}

void get_from_wait_queue(int *event_type, event_content_type **event_content, event_wait_queue *queue) {
	if (queue->top != NULL) {
		*event_type = queue->top->event_type;
		*event_content = queue->top->event_content;
		wait_event *previous_top = queue->top;
		queue->top = queue->top->next;
		free(previous_top);
		if (queue->top == NULL)
			queue->bottom = NULL;
	} else {
		*event_content = NULL;
		queue->top = NULL;
	}
}

int is_empty(event_wait_queue *queue) {
	if (queue->top == NULL)
		return 1;
	return 0;
}

