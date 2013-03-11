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
#ifndef _CONCURRENCY_CONTROL_H
#define _CONCURRENCY_CONTROL_H

#include "../application.h"

typedef struct _CC_event_list *CC_event_list_pointer;

typedef struct _CC_event_list {
	event_content_type * event;
	CC_event_list_pointer next;
} CC_event_list;

typedef struct _cc_metadata {
	transaction_metadata * active_transaction[ACTIVE_TRANSACTION_TABLE_SIZE];
	CC_event_list *event_queue;
	CC_event_list *event_queue_L1;
	int *locks;
	int lock_retry_num;
} cc_metadata;

#endif /* _CONCURRENCY_CONTROL_H */
