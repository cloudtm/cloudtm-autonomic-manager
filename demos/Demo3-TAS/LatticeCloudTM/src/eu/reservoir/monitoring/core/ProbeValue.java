// ProbeValue.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

/**
 * An interface for values that are in a Probe's Measurement.
 */
public interface ProbeValue {
    /**
     * Get the field no for an attribute.
     */
    public int getField();

    /**
     * Get the actual value.
     */
    public Object getValue();

    /**
     * Get the type for a ProbeValue.
     * e.g. Integer
     */
    public ProbeAttributeType getType();
}
