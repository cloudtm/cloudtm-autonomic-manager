// DHTInfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.im.dht;

import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttribute;
import eu.reservoir.monitoring.core.DataSourceDelegate;
import eu.reservoir.monitoring.core.DataSourceDelegateInteracter;
import eu.reservoir.monitoring.core.plane.InfoPlane;

import java.io.IOException;

/**
 * A DHTInfoPlane is an InfoPlane implementation
 * that sends the Information Model data.
 * It is also a DataSourceInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class DHTInfoPlane extends AbstractDHTInfoPlane implements InfoPlane, DataSourceDelegateInteracter {
    // DataSourceDelegate
    DataSourceDelegate dataSourceDelegate;

    // The hostname of the DHT root.
    String rootHost;

    // The port to connect to
    int rootPort;

    // The local port
    int port;

    /**
     * Construct a DHTInfoPlane.
     * Connect to the DHT root at hostname on port,
     * and start here on localPort.
     */
    public DHTInfoPlane(String remoteHostname, int remotePort, int localPort) {
	rootHost = remoteHostname;
	rootPort = remotePort;
	port = localPort;

	imNode = new IMNode(localPort, remoteHostname, remotePort);
    }

    /**
     * Connect to a delivery mechansim.
     * In a DHTInfoPlane we call announce.
     */
    public boolean connect() {
	if (super.connect()) {
	    return announce();
	} else {
	    return false;
	}
    }

    /**
     * Dicconnect from a delivery mechansim.
     * In a DHTInfoPlane we call dennounce.
     */
    public boolean disconnect() {
	if (super.disconnect()) {
	    return dennounce();
	} else {
	    return false;
	}
    }


    /**
     * Announce that the plane is up and running
     */
    public boolean announce() {
	try {
	    DataSource dataSource = dataSourceDelegate.getDataSource();
	    imNode.addDataSource(dataSource);

	    System.err.println("DHTInfoPlane: just announced DataSource " + dataSource);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Un-announce that the plane is up and running
     */
    public boolean dennounce() {
	// DataSource dataSource = dataSourceDelegate.getDataSource();
	// return imNode.removeDataSource(dataSource);
	return true;
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
	System.err.println("DHTInfoPlane: setDataSource: " + ds);
	dataSourceDelegate = ds;
	return ds;
    }

    /**
     * Add a DataSource
     */
    public boolean addDataSourceInfo(DataSource ds) {
	try {
	    imNode.addDataSource(ds);

	    System.err.println("DHTInfoPlane: just added DataSource " + ds);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Add a Probe
     */
    public boolean addProbeInfo(Probe p) {
	try {
	    imNode.addProbe(p);

	    System.err.println("DHTInfoPlane: just added Probe " + p);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }



    /**
     * Add a ProbeAttribute to a ProbeAttribute
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	try {
	    imNode.addProbeAttribute(p, pa);

	    System.err.println("DHTInfoPlane: just added ProbeAttribute " + p + "." + pa);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Modify a DataSource
     */
    public boolean modifyDataSourceInfo(DataSource ds) {
	try {
	    imNode.modifyDataSource(ds);

	    System.err.println("DHTInfoPlane: just modified DataSource " + ds);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Modify a Probe
     */
    public boolean modifyProbeInfo(Probe p) {
	try {
	    imNode.modifyProbe(p);

	    System.err.println("DHTInfoPlane: just modified Probe " + p);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Modify a ProbeAttribute from a Probe
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	try {
	    imNode.modifyProbeAttribute(p, pa);

	    System.err.println("DHTInfoPlane: just modified ProbeAttribute " + p + "." + pa);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }


    /**
     * Remove a DataSource
     */
    public boolean removeDataSourceInfo(DataSource ds) {
	try {
	    imNode.removeDataSource(ds);

	    System.err.println("DHTInfoPlane: just removed DataSource " + ds);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Remove a Probe
     */
    public boolean removeProbeInfo(Probe p) {
	try {
	    imNode.removeProbe(p);

	    System.err.println("DHTInfoPlane: just removed Probe " + p);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }

    /**
     * Remove a ProbeAttribute from a Probe
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	try {
	    imNode.removeProbeAttribute(p, pa);

	    System.err.println("DHTInfoPlane: just removed ProbeAttribute " + p + "." + pa);
	    return true;
	} catch (IOException ioe) {
	    return false;
	}
    }


 


}