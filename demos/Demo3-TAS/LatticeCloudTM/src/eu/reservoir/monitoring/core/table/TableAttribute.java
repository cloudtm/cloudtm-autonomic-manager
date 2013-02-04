// TableAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

import eu.reservoir.monitoring.core.ProbeAttributeType;
import java.io.Serializable;

/**
 * An interface for a TableAttribute.
 * These define the types for each column.
 */
public interface TableAttribute extends Serializable {
    /**
     * Get the column no for a TableAttribute.
     */
    public int getColumn();

    /**
     * Get the name for a TableAttribute.
     * e.g. used-memory
     */
    public String getName();

    /**
     * Get the type for a TableAttribute.
     * e.g. Integer
     */
    public ProbeAttributeType getType();
}