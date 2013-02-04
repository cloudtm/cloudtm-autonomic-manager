// ID.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import java.math.BigInteger;

/**
 * An ID in RESERVOIR monitoring.
 * The actual implementation is based on a BigInteger
 */
public class ID extends BigInteger {
    public ID(Number n) {
	super(n.toString());
    }
}
