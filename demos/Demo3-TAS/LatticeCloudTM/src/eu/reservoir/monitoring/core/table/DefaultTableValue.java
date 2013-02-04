// DefaultTableValue.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.TypeException;

/**
 * A default implementation of a TableValue.
 */
public class DefaultTableValue implements TableValue {
    // the value
    Object value;

    // the type
    ProbeAttributeType type;

   /**
     * Construct a TableValue
     * This throws a TypeException if the type of the value is not
     * one supported by ProbeAttributeType.
     */
    public DefaultTableValue(Object value) throws TypeException {
	// this throws an error if the typ is invalid
	this.type = ProbeAttributeType.lookup(value);

	this.value = value;
    }

    /**
     * Get the underlying value.
     */
    public Object getValue() {
	return value;
    }

    /**
     * Get the type for a TableValue
     * e.g. INTEGER
     */
    public ProbeAttributeType getType() {
	return type;
    }

    /**
     * To string
     */
    public String toString() {
	return "(" + type + " " + value + ")";
    }

}