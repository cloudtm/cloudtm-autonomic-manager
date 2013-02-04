// EveryNMillseconds.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.datarate;

/**
 * Every N milliseconds.
 * e.g. EveryNMilliseconds(5) means get a sample every 5 milliseconds.
 */
public class EveryNMilliseconds extends Timing {

    public EveryNMilliseconds(int gap) {
	// reciprocal(gap / (3600 * 1000))
	super(3600 * 1000, gap);
    }
}