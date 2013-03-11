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
#ifndef _CONCURRENCY_CONTROL_FUNCTIONS_H
#define _CONCURRENCY_CONTROL_FUNCTIONS_H

#include "../states.h"
#include "../transaction.h"

int remove_transaction_metadata(int tx_id, SERVER_lp_state_type * pointer);
transaction_metadata *get_transaction_metadata(int tx_id, SERVER_lp_state_type *pointer);
int add_transaction_metadata(int tx_id, int local, int tx_run_number, SERVER_lp_state_type * pointer);
int CC_control(event_content_type * event_content, state_type *state, time_type now);
void CC_init(state_type * pointer);
int remove_event_of_tx(CC_event_list * pointer, int txn_id);

#endif /* _CONCURRENCY_CONTROL_FUNCTIONS_H */
