// MlistValue.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2009

package eu.reservoir.monitoring.core.list;

import eu.reservoir.monitoring.core.ProbeAttributeType;
import java.io.Serializable;

/**
 * An interface for a value in a MList.
 */
public interface MListValue extends Serializable {
    /**
     * Get the actual value.
     */
    public Object getValue();

    /**
     * Get the type for a MListValue
     * e.g. INTEGER
     */
    public ProbeAttributeType getType();
}