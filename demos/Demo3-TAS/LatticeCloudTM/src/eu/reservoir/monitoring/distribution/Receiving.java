// Receiving.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution;

import eu.reservoir.monitoring.core.TypeException;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * An interface for distribution components that need
 * to do receiving from the transport.
 */
public interface Receiving {
    /**
     * This method is called just after a message
     * has been received from some underlying transport
     * at a particular multicast address.
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException;

    /**
     * This method is called just after there has been an error
     * in received from some underlying transport.
     * This passes the exception into the Receiving object.
     */
    public void error(Exception e);



}