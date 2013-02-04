// MeasurementReporting.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

/**
 * An interface for reporting from an object.
 */
public interface MeasurementReporting {
    /**
     * Set the object that will recieve the measurements.
     */
    public Object setMeasurementReceiver(MeasurementReceiver mr);

}
