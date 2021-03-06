/**
 *			Copyright (C) 2008-2012 HPDCS Group
 *			http://www.dis.uniroma1.it/~hpdcs
 *
 *
 * This file is part of ROOT-Sim (ROme OpTimistic Simulator).
 * 
 * ROOT-Sim is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * ROOT-Sim is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ROOT-Sim; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * @file ROOT-Sim.h
 * @brief This header defines all the symbols which are needed to develop a Model
 *        to be simulated on top of ROOT-Sim.
 * @author Francesco Quaglia
 * @author Alessandro Pellegrini
 * @author Roberto Vitali
 * @date 3/16/2011
 */

#pragma once
#ifndef __ROOT_Sim_H
#define __ROOT_Sim_H

#include <stdlib.h>
#include <string.h>
#include <float.h>

#ifdef INIT
#undef INIT
#endif
/// This is the message code which is sent by the simulation kernel upon startup
#define INIT	0

#ifdef bool
#undef bool
#endif
/// This is to give boolean type to the old C 
enum _bool {
	false, true
};
typedef enum _bool bool;

/// This macro can be used to convert command line parameters to integers
#define parseInt(s) ({\
			int __value;\
			char *__endptr;\
			__value = (int)strtol(s, &__endptr, 10);\
			if(!(*s != '\0' && *__endptr == '\0')) {\
				fprintf(stderr, "%s:%d: Invalid conversion value: %s\n", __FILE__, __LINE__, s);\
			}\
			__value;\
		     })

/// This macro can be used to convert command line parameters to doubles
#define parseDouble(s) ({\
			double __value;\
			char *__endptr;\
			__value = strtod(s, &__endptr);\
			if(!(*s != '\0' && *__endptr == '\0')) {\
				fprintf(stderr, "%s:%d: Invalid conversion value: %s\n", __FILE__, __LINE__, s);\
			}\
			__value;\
		       })

/// This macro can be used to convert command line parameters to floats
#define parseFloat(s) ({\
			float __value;\
			char *__endptr;\
			__value = strtof(s, &__endptr);\
			if(!(*s != '\0' && *__endptr == '\0')) {\
				fprintf(stderr, "%s:%d: Invalid conversion value: %s\n", __FILE__, __LINE__, s);\
			}\
			__value;\
		       })

/// This macro can be used to convert command line parameters to booleans
#define parseBoolean(s) ({\
			bool __value;\
			if(strcmp((s), "true") == 0) {\
				__value = true;\
			} else {\
				__value = false;\
			}\
			__value;\
		       })

/// This macro can be used to retrieve a command line parameter within INIT event
#define getParameter(event_content, idx) (((char **)event_content)[(idx)])

/// This macro can be used to check a command line parameter within INIT event
#define checkParameter(s, event_content, idx) (strcmp(getParameter(event_content, idx), s) == 0)

/// This defines the type with whom timestamps are represented
typedef double time_type;

/// Infinite timestamp: this is the highest timestamp in a simulation
#define INFTY DBL_MAX

/// This is the definition of the number of LPs running in the current simulation
extern unsigned int n_prc_tot;

double Random(void);
int RandomRange(int min, int max);
double Expent(double mean);
double Normal(void);
double Gamma(int ia);
double Poisson(void);
int Zipf(double skew, int limit);

// Expose to the application level the ordering library
#define ORDER_DESCENDING	9781
#define ORDER_ASCENDING		9782
extern void QuickSort(int *array, int elements, int order);
extern void QuickSort_Struct(void *array, int elements, int element_size, int order);

void ScheduleNewEvent(unsigned int receiver, time_type timestamp, unsigned int event_type, void *event_content, unsigned int event_size);
double ProcessedEventsPerSecond();
void SetState(void *new_state);

#endif /* __ROOT_Sim_H */

