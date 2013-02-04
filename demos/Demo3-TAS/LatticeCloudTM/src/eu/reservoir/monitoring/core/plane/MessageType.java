// MessageType.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core.plane;

import java.util.HashMap;
import java.util.EnumSet;

/**
 * The types of message allowed in the Data Plane.
 */
public enum MessageType {
    /*
     * A data source announces its
     * existence, so everyone knows it is available.
     */
    ANNOUNCE(101),

    /*
     * A data source sends a measurement.
     */
    MEASUREMENT(102);

    /*
     * A lookup table so we can go from value to enum.
     */
    private static final HashMap<Integer, MessageType> lookup = new HashMap<Integer, MessageType>();

    // fill in the map
    static {
	for(MessageType t : EnumSet.allOf(MessageType.class))
	    lookup.put(t.getValue(), t);
    }

    /**
     * Lookup an MessageType enum 
     */
    public static final MessageType lookup(int mt) {
	return lookup.get(mt);
    }
     
    /*
     * The value for each MessageType.
     */
    int value = 0;

    /*
     * Define the message types
     */
    private MessageType(int v) {
	value = v;
    }

    /**
     * Get the value of a MessageType
     */
    public int getValue() {
	return value;
    }





}