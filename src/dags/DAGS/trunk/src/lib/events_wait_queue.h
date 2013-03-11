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
#ifndef _EVENTS_WAIT_QUEUE_H
#define _EVENTS_WAIT_QUEUE_H

#include <stdlib.h>
#include "../configuration.h"
#include "../application.h"

typedef struct _wait_event {
	int event_type;
	event_content_type *event_content;
	double insertion_time;
	struct _wait_event *next;
} wait_event;

typedef struct _event_wait_queue {
	wait_event *top;
	wait_event *bottom;
} event_wait_queue;

void copy_into_wait_queue(int event_type, event_content_type *event_content, time_type now, event_wait_queue *queue);
void get_from_wait_queue(int *event_type, event_content_type **event_content, event_wait_queue *queue);
int is_empty(event_wait_queue *queue);
int get_queue_length(event_wait_queue *queue);

#endif /* _EVENTS_WAIT_QUEUE_H */
