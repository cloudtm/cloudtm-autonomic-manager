// MeasurementException.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package eu.reservoir.monitoring.core;

/**
 * An exception thrown where there is a problem with a Measurement.
 */
public class MeasurementException extends RuntimeException {
    /**
     * Construct a MeasurementException with a message.
     */
    public MeasurementException(String msg) {
	super(msg);
    }

    /**
     * Construct a MeasurementException.
     */
    public MeasurementException() {
	super();
    }

}