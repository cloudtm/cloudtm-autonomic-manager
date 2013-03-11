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

#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "../configuration.h"

#define MAX_SEED 30000

extern unsigned int cur_me;

#ifdef __x86_64
#define  NORM     0x7fffffffffffffff
#endif

#ifdef i386
#define  NORM     0x7fffffff
#endif

typedef unsigned long seed_type;

static seed_type lp_seed[NUM_OBJECTS];
static seed_type master_seed;

#define RND_IA	 16807
#define RND_IM	 2147483647
#define RND_AM	 (1.0/RND_IM)
#define RND_IQ	 127773
#define RND_IR	 2836
#define RND_MASK 123459876

/**
 * This function returns a number in between [0,1], according to a Uniform Distribution.
 *
 * @author S.K. Park
 * @author K.W. Miller
 * @author Alessandro Pellegrini
 * @author Roberto Vitali
 * @param pseed A pointed to the seed
 * @return A random number, in between [0,1]
 * @date 3/16/2011
 */

double Random(void) {
	unsigned long k;
	double ans;
	seed_type *idum;

	// If some subsystem needs a random number not associated with any lp (e.g. at start-up time)
	idum = &(lp_seed[cur_me]);

	*idum ^= RND_MASK;
	k = (*idum) / RND_IQ;
	*idum = RND_IA * (*idum - k * RND_IQ); // - RND_IR * k;
	ans = RND_AM * (*idum);
	*idum ^= RND_MASK;

	return ans;

}
#undef RND_MASK
#undef RND_IR
#undef RND_IQ
#undef RND_AM
#undef RND_IM
#undef RND_IA

int RandomRange(int min, int max) {
	return (int) floor(Random() * (max - min + 1)) + min;
}

int RandomRangeNonUniform(int x, int min, int max) {
	return (((RandomRange(0, x) | RandomRange(min, max))) % (max - min + 1)) + min;
}

/**
 * This function returns a random number according to an Exponential distribution.
 * The mean value of the distribution must be passed as the mean value.
 *
 * @author Alessandro Pellegrini
 * @param mean Mean value of the distribution
 * @return A random number
 * @date 3/16/2011
 */
double Expent(double mean) {

	return (-mean * log(1 - Random()));
}

/**
 * This function returns a number in according to a Normal Distribution with mean 0
 *
 * @author
 * @return A random number
 * @date 4/20/2011
 */
double Normal(void) {
	static bool iset = false;
	static double gset;
	double fac, rsq, v1, v2;

	if (iset == false) {
		do {
			v1 = 2.0 * Random() - 1.0;
			v2 = 2.0 * Random() - 1.0;
			rsq = v1 * v1 + v2 * v2;
		} while (rsq >= 1.0 || rsq != 0);

		fac = sqrt(-2.0 * log(rsq) / rsq);

		// Perform Box-Muller transformation to get two normal deviates. Return one
		// and save the other for next time.
		gset = v1 * fac;
		iset = true;
		return v2 * fac;
	} else {
		// A deviate is already available
		iset = false;
		return gset;
	}
}

/**
 * This function returns a number in according to a Gamma Distribution of Integer Order ia,
 * a waiting time to the ia-th event in a Poisson process of unit mean.
 *
 * @author D.E. Knuth
 * @param ia Integer Order of the Gamma Distribution
 * @return A random number
 * @date 4/20/2011
 */

double Gamma(int ia) {
	int j;
	double am, e, s, v1, v2, x, y;

	if (ia < 1) {
		//rootsim_error(false, "Gamma distribution must have a ia value >= 1. Defaulting to 1...");
		ia = 1;
	}

	if (ia < 6) {
		// Use direct method, adding waiting times
		x = 1.0;
		for (j = 1; j <= ia; j++)
			x *= Random();
		x = -log(x);
	} else {
		// Use rejection method
		do {
			do {
				do {
					v1 = Random();
					v2 = 2.0 * Random() - 1.0;
				} while (v1 * v1 + v2 * v2 > 1.0);

				y = v2 / v1;
				am = (double) (ia - 1);
				s = sqrt(2.0 * am + 1.0);
				x = s * y + am;
			} while (x < 0.0);

			e = (1.0 + y * y) * exp(am * log(x / am) - s * y);
		} while (Random() > e);
	}

	return x;
}

/**
 * This function returns the waiting time to the next event in a Poisson process of unit mean.
 *
 * @author Alessandro Pellegrini
 * @param ia Integer Order of the Gamma Distribution
 * @return A random number
 * @date 11 Jan 2012
 */
double Poisson(void) {
	return Gamma(1);
}

/**
 * This function returns a random sample from a Zipf distribution.
 * Based on the rejection method by Luc Devroye for sampling:
 * "Non-Uniform Random Variate Generation, page 550, Springer-Verlag, 1986
 *
 * @author Alessandro Pellegrini
 * @param skew The skew of the distribution
 * @param limit The largest sample to retrieve
 * @return A random number
 * @date 8 Nov 2012
 */
int Zipf(double skew, int limit) {
	double a = skew;
	double b = pow(2., a - 1.);
	double x, t, u, v;
	do {
		u = Random();
		v = Random();
		x = floor(pow(u, -1. / a - 1.));
		t = pow(1. + 1. / x, a - 1.);
	} while (v * x * (t - 1.) / (b - 1.) > (t / b) || x > limit);
	return (int) x;
}

seed_type load_seed(void) {

	seed_type seed;
	seed_type new_seed;

	FILE *fp;

	if ((fp = fopen("random.conf", "r+")) == NULL)
		fprintf(stderr, "Unable to load numerical distribution configuration: %s. Check your installation!", "random.conf");

	// Load the initial seed
	fscanf(fp, "%lu", &seed);

	rewind(fp);
	srandom(seed);
	new_seed = random();
	//add a comment to the following line code for deterministic runs
	fprintf(fp, "%lu\n", new_seed);

	fclose(fp);

	return seed;
}

#define RS_WORD_LENGTH (8 * sizeof(unsigned long))
#define ROR(value, places) (value << (places)) | (value >> (RS_WORD_LENGTH - places)) // Circular shift
void numerical_init(void) {

	master_seed = load_seed();

	unsigned int i;

	// Initialize the per-LP seed
	for (i = 0; i < NUM_OBJECTS; i++)
		lp_seed[i] = ROR(master_seed, i);

}
#undef RS_WORD_LENGTH
#undef ROR
