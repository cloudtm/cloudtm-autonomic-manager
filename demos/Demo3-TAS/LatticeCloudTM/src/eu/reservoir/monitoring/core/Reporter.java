// Reporter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.core;

import java.util.EventListener;

/**
 * An interface for printing a Measurement.
 * Reporters behave like an EventListener.
 */
public interface Reporter extends EventListener {
    /**
     * Report the object
     */
    public void report(Measurement m);

}
