// TableException.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

/**
 * An exception thrown where there is a problem with a Table.
 */
public class TableException extends Exception {
    /**
     * Construct a TableException with a message.
     */
    public TableException(String msg) {
	super(msg);
    }

    /**
     * Construct a TableException.
     */
    public TableException() {
	super();
    }

}