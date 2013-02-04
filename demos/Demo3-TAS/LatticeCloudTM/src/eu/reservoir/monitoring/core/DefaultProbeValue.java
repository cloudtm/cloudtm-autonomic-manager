// DefaultProbeValue.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import java.io.Serializable;

/**
 * A default implementation of a ProbeValue.
 */
public class DefaultProbeValue implements ProbeValue, Serializable {
    int field = 0;
    Object value;
    ProbeAttributeType type;

   /**
     * Construct a ProbeValue
     */
    public DefaultProbeValue(int field, Object value) throws TypeException {
	this.field = field;
	this.value = value;
	type =  ProbeAttributeType.lookup(value);
    }

    /**
     * Get the field no for an attribute.
     */
    public int getField() {
	return field;
    }

    /**
     * Get the value for an attribute.
     */
    public Object getValue() {
	return value;
    }

    /**
     * Get the type for a ProbeValue.
     * e.g. INTEGER
     */
    public ProbeAttributeType getType() {
	return type;
    }

    /**
     * To string
     */
    public String toString() {
	return field + ": " + type + " " + value;
    }

}