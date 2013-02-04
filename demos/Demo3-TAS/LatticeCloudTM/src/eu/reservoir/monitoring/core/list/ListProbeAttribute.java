// ListProbeAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2009

package eu.reservoir.monitoring.core.list;

import eu.reservoir.monitoring.core.ProbeAttribute;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.AbstractProbeAttribute;

/**
 * An implementation for having a list as a ProbeAttribute
 * These are a Probe's Data Dictionary.
 */
public class ListProbeAttribute extends AbstractProbeAttribute implements ProbeAttribute {
    // The type of the elements
    ProbeAttributeType elementType;

    /**
     * Construct a ProbeAttribute.
     */
    public ListProbeAttribute(int field, String name, ProbeAttributeType type) {
	super(field, name, ProbeAttributeType.LIST, "_LIST_");
	this.elementType = type;
    }

}