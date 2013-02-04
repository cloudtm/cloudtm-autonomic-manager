// SamplesPerMinute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.datarate;

/**
 * Specifiy a number of samples per minute.
 */
public class SamplesPerMinute extends Timing {

    public SamplesPerMinute(int samples) {
	super(samples * 60, 1);
    }
}