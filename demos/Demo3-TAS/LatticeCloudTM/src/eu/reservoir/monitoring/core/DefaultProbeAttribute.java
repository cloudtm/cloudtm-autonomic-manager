// DefaultProbeAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

/**
 * A default implementation of a ProbeAttribute.
 * These are a Probe's Data Dictionary.
 */
public class DefaultProbeAttribute extends AbstractProbeAttribute implements ProbeAttribute {
    /**
     * Construct a ProbeAttribute.
     */
    public DefaultProbeAttribute(int field, String name, ProbeAttributeType type, String units) {
	super(field, name, type, units);

	if (type == ProbeAttributeType.TABLE) {
	    throw new Error("Table type not allowable in DefaultProbeAttribute");
	} else if (type == ProbeAttributeType.LIST) {
	    throw new Error("List type not allowable in DefaultProbeAttribute");
	}
    }

}
