// TypeException.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core;

/**
 * An exception thrown where there is a problem with a ProbeAttributeType.
 */
public class TypeException extends Exception {
    /**
     * Construct a TypeException with a message.
     */
    public TypeException(String msg) {
	super(msg);
    }

    /**
     * Construct a TypeException.
     */
    public TypeException() {
	super();
    }

}