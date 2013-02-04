// DataService.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.core.plane;

/**
 * An interface for sending measurement data.
 */
public interface DataService {

    /**
     * Send a message.
     */
    public int sendData(DataPlaneMessage dpm) throws Exception;

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean sentData(int id);

}
