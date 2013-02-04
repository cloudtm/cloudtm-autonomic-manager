// AbstractProbeAttribute.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core;

/**
 * An abstract implementation of a ProbeAttribute.
 */
public abstract class AbstractProbeAttribute implements ProbeAttribute {
    int field = 0;

    String name;

    ProbeAttributeType type;

    String units;

    /**
     * Construct a ProbeAttribute.
     */
    public AbstractProbeAttribute(int field, String name, ProbeAttributeType type, String units) {
	this.field = field;
	this.name = name;
	this.type = type;
	this.units = units;
    }

    /**
     * Get the field no for an attribute.
     */
    public int getField() {
	return field;
    }

    /**
     * Get the name for an attribute.
     * e.g. used-memory
     */
    public String getName() {
	return name;
    }

    /**
     * Get the type for an attribute.
     * e.g. Integer
     */
    public ProbeAttributeType getType() {
	return type;
    }

    /**
     * Get the units for an attribute.
     * e.g. Mb, Gb, Kbps
     */
    public String getUnits() {
	return units;
    }

    /**
     * To String.
     */
    public String toString() {
	return (field + ": " + name + ": " + type +  ": " + units);
    }
}
