// EveryNMinutes.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.datarate;

/**
 * Every N minutes.
 * e.g. EveryNMinutes(5) means get a sample every 5 minutes.
 */
public class EveryNMinutes extends Timing {

    public EveryNMinutes(int gap) {
	// reciprocal(gap / 60)
	super(60, gap);
    }
}