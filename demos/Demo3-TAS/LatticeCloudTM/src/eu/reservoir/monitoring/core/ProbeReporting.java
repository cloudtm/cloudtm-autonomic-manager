// ProbeReporting.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;


/**
 * An interface for reporting.
 */
public interface ProbeReporting {
    /**
     * Collect a measurment for the reciever
     */
    public ProbeMeasurement collect();

    /**
     * Inform the Probe of an object.
     */
    public Object inform(Object obj);

    /**
     * Called when there is an error in a Measurement.
     * By default it prints a stack trace.
     */
    public void error(MeasurementException me);

    /**
     * Get the current filter.
     */
    public ProbeFilter getProbeFilter();

    /**
     * Set the current filter.
     * Returns the previous filter.
     */
    public ProbeFilter setProbeFilter(ProbeFilter f);

    /**
     * Turn on filtering.
     */
    public ProbeReporting turnOnFiltering();

    /**
     * Turn off filtering.
     */
    public ProbeReporting turnOffFiltering();

    /**
     * Is the probe filtering values.
     */
    public boolean isFiltering();

}
