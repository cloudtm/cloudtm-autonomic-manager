// DefaultTableAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

import eu.reservoir.monitoring.core.ProbeAttributeType;

/**
 * A default implementation of a TableAttribute.
 */
public class DefaultTableAttribute implements TableAttribute {
    int column = 0;

    String name;

    ProbeAttributeType type;

    /**
     * Construct a TableAttribute.
     */
    public DefaultTableAttribute(int column, String name, ProbeAttributeType type) {
	this.column = column;
	this.name = name;
	this.type = type;
    }

    /**
     * Get the column no for a TableAttribute.
     */
    public int getColumn() {
	return column;
    }

    /**
     * Get the name for a TableAttribute.
     * e.g. used-memory
     */
    public String getName() {
	return name;
    }

    /**
     * Get the type for a TableAttribute.
     * e.g. Integer
     */
    public ProbeAttributeType getType() {
	return type;
    }

    /**
     * To String.
     */
    public String toString() {
	return "<" + column + ": " + name + ": " + type + ">";
    }
}
