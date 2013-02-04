// ProbeAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

/**
 * An interface for Attributes that are in a Probe's Data Dictionary.
 */
public interface ProbeAttribute {
    /**
     * Get the field no for an attribute.
     */
    public int getField();

    /**
     * Get the name for an attribute.
     * e.g. used-memory
     */
    public String getName();

    /**
     * Get the type for an attribute.
     * e.g. Integer
     */
    public ProbeAttributeType getType();
    /**
     * Get the units for an attribute.
     * e.g. Mb, Gb, Kbps
     */
    public String getUnits();
}
