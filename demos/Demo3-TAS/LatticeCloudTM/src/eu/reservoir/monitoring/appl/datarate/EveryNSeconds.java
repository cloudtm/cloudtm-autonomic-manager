// EveryNSeconds.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.datarate;

/**
 * Every N seconds.
 * e.g. EveryNSeconds(5) means get a sample every 5 seconds.
 */
public class EveryNSeconds extends Timing {

    public EveryNSeconds(int gap) {
	// reciprocal(gap/3600)
	super(3600, gap);
    }
}