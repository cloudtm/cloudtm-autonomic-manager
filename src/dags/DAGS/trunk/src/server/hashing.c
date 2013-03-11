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
#include "hashing.h"

int is_owner(int key, int server_id, int num_servers, int num_clients, int object_replication_degree) {

	int server_position = server_id - num_clients;
	if (object_replication_degree == 1) { //no replication
		if (((key % num_servers) == server_position))
			return 1;
		else
			return 0;
	} else {
		// partial/full replication
		int first_server = (key % num_servers);
		int last_server = (first_server + (object_replication_degree - 1)) % num_servers;
		if (first_server < last_server)
			if (first_server <= server_position && server_position <= last_server)
				return 1;
			else
				return 0;
		else // (first_server>=last_server)
		if (server_position <= last_server || server_position >= first_server)
			return 1;
		else
			return 0;
	}

}

int is_primary(int key, int server_id, int num_servers, int num_clients) {
	if (((key % num_servers) + num_clients == server_id))
		return 1;
	else
		return 0;
}
