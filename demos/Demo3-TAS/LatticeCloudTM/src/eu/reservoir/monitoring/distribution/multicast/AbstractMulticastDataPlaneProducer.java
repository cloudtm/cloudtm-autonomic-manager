// AbstractMulticastDataPlaneProducer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package eu.reservoir.monitoring.distribution.multicast;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.DataSourceDelegate;
import eu.reservoir.monitoring.core.DataSourceDelegateInteracter;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.*;
import eu.reservoir.monitoring.distribution.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A MulticastDataPlaneProducer is a DataPlane implementation
 * that sends Measurements by multicast.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public abstract class AbstractMulticastDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, Transmitting {
    // The address we are sending to
    MulticastAddress address;

    // DataSourceDelegate
    DataSourceDelegate dataSourceDelegate;

    // The MulticastTransmitter
    MulticastTransmitter mcastTransmitter;

    /**
     * Construct a MulticastDataPlaneProducer.
     */
    public AbstractMulticastDataPlaneProducer(MulticastAddress addr) {
	// sending address
	address = addr;
    }


    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (mcastTransmitter == null) {
		// Now connect to the new multicast address
		// Try a ttl of 64
		MulticastTransmitter tt = new MulticastTransmitter(this, address, 64);
		tt.join();
		
		mcastTransmitter = tt;

		return true;
	    } else {
		return true;
	    }

	} catch (IOException ioe) {
	    // Current implementation will be to do a stack trace
	    ioe.printStackTrace();

	    return false;
	}

    }

    /**
     * Disconnect from a delivery mechansim.
     */
    public boolean disconnect() {
	try {
	    mcastTransmitter.leave();
	    mcastTransmitter = null;
	    return true;
	} catch (IOException ieo) {
	    mcastTransmitter = null;
	    return false;
	}
    }

    /**
     * Announce that the plane is up and running
     */
    public boolean announce() {
	// do nothing currenty
	return true;
    }

    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce() {
	// do nothing currenty
	return true;
    }

    /**
     * Send a message onto the multicast address.
     * The message is XDR encoded and it's structure is:
     * +-------------------------------------------------------------------+
     * | data source id (long) | msg type (int) | seq no (int) | payload   |
     * +-------------------------------------------------------------------+
     */
    abstract public int transmit(DataPlaneMessage dpm) throws Exception;
    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean transmitted(int id) {
	sentData(id);
	return true;
    }

    /**
     * Send a message.
     */
    public int sendData(DataPlaneMessage dpm) throws Exception {
	return transmit(dpm);
    }

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean sentData(int id) {
	return dataSourceDelegate.sentData(id);
    }

    /**
     * Receiver of a measurment
     */
    public Measurement report(Measurement m) {
	// currently do nothing
	return null;
    }

    /**
     * Get the dataSourceDelegate this is a delegate for.
     */
    public DataSourceDelegate getDataSourceDelegate() {
	return dataSourceDelegate;
    }

    /**
     * Set the dataSourceDelegate this is a delegate for.
     */
    public DataSourceDelegate setDataSourceDelegate(DataSourceDelegate ds) {
	dataSourceDelegate = ds;
	return ds;
    }

}