// TableProbeAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

import eu.reservoir.monitoring.core.ProbeAttribute;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.AbstractProbeAttribute;

/**
 * An implementation for having a table as a ProbeAttribute
 * These are a Probe's Data Dictionary.
 */
public class TableProbeAttribute extends AbstractProbeAttribute implements ProbeAttribute {
    /*
     * The TableHeader acts as the definition.
     */
    TableHeader definition;

    /**
     * Construct a ProbeAttribute.
     */
    public TableProbeAttribute(int field, String name,  TableHeader definition) {
	super(field, name, ProbeAttributeType.TABLE, "_TABLE_");
	this.definition = definition;
    }

}