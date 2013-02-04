// EveryNHours.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.datarate;

/**
 * Every N hours
 * e.g. EveryNHours(5) means get a sample every 5 hours
 */
public class EveryNHours extends Timing {

    public EveryNHours(int gap) {
	// reciprocal(gap/1)
	super(1, gap);
    }
}