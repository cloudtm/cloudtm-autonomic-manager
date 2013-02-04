// Timestamp.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Jan 2009

package eu.reservoir.monitoring.core;

import java.io.Serializable;

/**
 * A timestamp.
 */
public class Timestamp implements Serializable {
    /**
     * the value of a timestamp.
     */
    long timestamp = 0;


    /**
     * Construct a Timestamp.
     */
    public Timestamp(long t) {
	timestamp = t;
    }

    /**
     * Get the value of a timestamp.
     */
    public long value() {
	return timestamp;
    }

    /**
     * Do this_timestamp - other_timestamp.
     */
    public Timestamp minus(Timestamp t) {
	if (t != null) {
	    return new Timestamp(timestamp - t.value());
	} else {
	    return new Timestamp(0);
	}
    }

    /**
     * Do this_timestamp + other_timestamp.
     */
    public Timestamp plus(Timestamp t) {
	if (t != null) {
	    return new Timestamp(timestamp + t.value());
	} else {
	    return new Timestamp(0);
	}
    }

    /**
     * To String.
     */
    public String toString() {
	return "" + timestamp;
    }
}