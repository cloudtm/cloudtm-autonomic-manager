// Transmitting.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution;

import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.DataPlaneMessage;
import java.io.IOException;

/**
 * An interface for distribution components that need
 * to do transmitting of DataPlaneMessage objects.
 */
public interface Transmitting {
    /**
     * Send a message onto the multicast address.
     */
    public int transmit(DataPlaneMessage dpm) throws Exception; //IOException, TypeException;

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean transmitted(int id);
}