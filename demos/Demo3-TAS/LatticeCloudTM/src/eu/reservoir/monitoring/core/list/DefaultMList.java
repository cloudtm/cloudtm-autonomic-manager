// DefaultMList.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2009

package eu.reservoir.monitoring.core.list;

import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.TypeException;
import java.util.List;
import java.util.LinkedList;

/**
 * A default implementation of a MList.
 */
public class DefaultMList implements MList {
    // The list type
    ProbeAttributeType listType;

    // A list of MListValues that are part of this list
    List<MListValue> values;

   /**
    * Construct a MList.
    */
    public DefaultMList(ProbeAttributeType listType) {
	values = new LinkedList<MListValue>();
	this.listType = listType;
    }

    /**
     * Get the size of the list.
     */
    public int size() {
	return values.size();
    }

    /**
     * Get the Nth element from a list.
     */
    public MListValue get(int n) {
	return values.get(n);
    }

    /**
     * Set the Nth element of a list.
     */
    public MList set(int n, MListValue value) throws TypeException {
	// set value in Nth position
	ProbeAttributeType valueType = value.getType();

	if (listType.equals(valueType)) {
	    values.add(n, value);
	    return this;
	} else {
	    throw new TypeException("MListValue at position: " + n +
				    " cannot be of type " + valueType +
				    " in list of type " + listType);
	}
    }

    /**
     * Add some data value to the list.
     */
    public MList add(MListValue value) throws TypeException {
	ProbeAttributeType valueType = value.getType();

	if (listType.equals(valueType)) {
	    int n = size();
	    values.add(n, value);
	    return this;
	} else {
	    throw new TypeException("MListValue at position: " + size() +
				    " cannot be of type " + valueType +
				    " in list of type " + listType);
	}
    }

    /**
     * Add an object value to the list.
     * This throws an Error if the type of the value is not
     * one supported by ProbeAttributeType.
     */
    public MList add(Object value) throws TypeException {
	ProbeAttributeType valueType = ProbeAttributeType.lookup(value);

	if (listType.equals(valueType)) {
	    int n = size();
	    values.add(n, new DefaultMListValue(value));
	    return this;
	} else {
	    throw new TypeException("Value at position: " + size() +
				    " cannot be of type " + valueType +
				    " in list of type " + listType);
	}
    }

    /**
     * Convert the MList to a List.
     */
    public List<MListValue> toList() {
	return values;
    }

    /**
     * Get the type of the elements for a MList.
     * e.g. INTEGER
     */
    public ProbeAttributeType getType() {
	return listType;
    }

    /**
     * To string
     */
    public String toString() {
	return values.toString();
    }

}