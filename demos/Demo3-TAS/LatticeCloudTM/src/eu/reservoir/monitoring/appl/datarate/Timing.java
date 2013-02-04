// SamplesPerHour.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.datarate;

import eu.reservoir.monitoring.core.Rational;

/**
 * A version of Rational used for timing.
 */
public class Timing extends Rational {
    /**
     * Construct a Timing object.
     */
    public Timing(int n, int d) {
	super(n, d);
    }

    /**
     * Get the timing gap in milliseconds.
     */
    public long asMilliseconds() {
	return (long)(this.reciprocal().toDouble() * 3600 * 1000);
    }
}