// AbstractUDPDataPlaneProducer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package eu.reservoir.monitoring.distribution.udp;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.DataSourceDelegate;
import eu.reservoir.monitoring.core.DataSourceDelegateInteracter;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.*;
import eu.reservoir.monitoring.distribution.*;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetSocketAddress;

/**
 * An AbstractUDPDataPlaneProducer is a DataPlane implementation
 * that sends Measurements by UDP.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public abstract class AbstractUDPDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, Transmitting {
    // The address we are sending to
    InetSocketAddress address;

    // The UDPTransmitter
    UDPTransmitter udpTransmitter;

    // DataSourceDelegate
    DataSourceDelegate dataSourceDelegate;

    /**
     * Construct an AbstractUDPDataPlaneProducer.
     */
    public AbstractUDPDataPlaneProducer(InetSocketAddress addr) {
	// sending address
	address = addr;
    }


    /**
     * Connect to a delivery mechansim.
     */
     public boolean connect() {
	try {
	    // only connect if we're not already connected
	    if (udpTransmitter == null) {
		// Now connect to the IP address
		UDPTransmitter tt = new UDPTransmitter(this, address);

		tt.connect();
		
		udpTransmitter = tt;

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

//     public boolean connect() {
// 	return producer.connect();
//     }

    /**
     * Disconnect from a delivery mechansim.
     */
    public boolean disconnect() {
	try {
	    udpTransmitter.end();
	    udpTransmitter = null;
	    return true;
	} catch (IOException ieo) {
	    udpTransmitter = null;
	    return false;
	}
    }

//     public boolean disconnect() {
// 	return producer.disconnect();
//     }

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
     * Send a message onto the address.
     * The message is XDR encoded and it's structure is:
     * +-------------------------------------------------------------------+
     * | data source id (long) | msg type (int) | seq no (int) | payload   |
     * +-------------------------------------------------------------------+
     */
    public abstract int transmit(DataPlaneMessage dsp) throws Exception;


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
	return true;
    }

    /**
     * Receiver of a measurment, with an extra object that has context info
     */
    public Measurement report(Measurement m) {
	// currently do nothing
	return null;
    }

    /**
     * Get the DataSourceDelegate this is a delegate for.
     */
    public DataSourceDelegate getDataSourceDelegate() {
	return dataSourceDelegate;
    }

    /**
     * Set the DataSourceDelegate this is a delegate for.
     */
    public DataSourceDelegate setDataSourceDelegate(DataSourceDelegate ds) {
	dataSourceDelegate = ds;
	return ds;
    }

}