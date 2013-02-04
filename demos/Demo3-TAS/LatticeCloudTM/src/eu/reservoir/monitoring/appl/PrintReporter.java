// PrintReporter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.appl;

import eu.reservoir.monitoring.core.Reporter;
import eu.reservoir.monitoring.core.Measurement;


/**
 * A PrintReporter just prints a Measurement.
 */
public class PrintReporter implements Reporter {
    /**
     * In a PrintReporter, report() just prints the Measurement.
     */
    public void report(Measurement m) {
	System.out.println(m);
    }
}