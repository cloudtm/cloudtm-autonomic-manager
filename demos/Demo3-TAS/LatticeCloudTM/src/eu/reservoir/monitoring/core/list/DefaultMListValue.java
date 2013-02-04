// DefaultMListValue.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2009

package eu.reservoir.monitoring.core.list;

import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.TypeException;

/**
 * A default implementation of a MListValue.
 */
public class DefaultMListValue implements MListValue {
    // the value
    Object value;

    // the type
    ProbeAttributeType type;

   /**
     * Construct a MListValue
     * This throws a TypeException if the type of the value is not
     * one supported by ProbeAttributeType.
     */
    public DefaultMListValue(Object value) throws TypeException {
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
     * Get the type for a MListValue
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