// IMNode.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.im.dht;

import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttribute;
import eu.reservoir.monitoring.core.ProbeManager;
import eu.reservoir.monitoring.core.ID;
import java.io.Serializable;
import java.io.IOException;
import java.util.Map;
import java.util.Collection;
import java.math.BigInteger;

/**
 * An IMNode is responsible for converting  DataSource and Probe
 * attributes into Hashtable keys and values for the DistributedHashTable.
 * <p>
 * For example, with a given DataSource you get:
 * <ul>
 * <li> /datasource/datasource-id/attribute = value
 * </ul>
 * and with a given Probe you get:
 * <ul>
 * <li> /probe/probe-id/attribute = value
 * </ul>
 */
public class IMNode {
    // The actual DHT
    DistributedHashTable dht = null;

    // the local port
    int localPort = 0;

    // the remote host
    String remoteHost;

    // the remote port
    int remotePort = 0;

    /**
     * Construct an IMNode, given a local port and a remote host
     * and a remote port.
     */
    public IMNode(int myPort, String remHost, int remPort) {
	localPort = myPort;
	remoteHost = remHost;
	remotePort = remPort;
    }

    /**
     * Connect to the DHT peers.
     */
    public boolean connect() {
	try {
	    // only connect if we don't already have a DHT
	    if (dht == null) {
		dht = new DistributedHashTable(localPort);
		dht.connect(remoteHost, remotePort);

		System.err.println("IMNode: connect: " + localPort + " to " + remoteHost + "/" + remotePort);

		return true;
	    } else {
		return true;
	    }
	} catch (IOException ioe) {
	    System.err.println("IMNode: connect failed: " + ioe);
	    if (dht != null) {
		try {
		    dht.close();
		} catch (IOException e) {
		}
		dht = null;
	    }
	    return false;
	}
    }

    /**
     * Disconnect from the DHT peers.
     */
    public boolean disconnect() {
	try {
	    dht.close();
	    dht = null;
	    return true;
	} catch (IOException e) {
	    dht = null;
	    return false;
	}
    }

    /**
     * Add data for a DataSource
     */
    public IMNode addDataSource(DataSource ds) throws IOException {
	putDHT("/datasource/" + ds.getID() + "/name", ds.getName());

	Collection<Probe> probes = ds.getProbes();

	// skip through all probes
	for (Probe aProbe : probes) {
	    addProbe(aProbe);
	}
	    
	return this;
    }

    /**
     * Add data for a Probe.
     */
    public IMNode addProbe(Probe aProbe) throws IOException {
	// add probe's ref to its data source
	// found through the ProbeManager
	DataSource ds = (DataSource)aProbe.getProbeManager();
	putDHT("/probe/" + aProbe.getID() + "/datasource", ds.getID());

	// add probe name to DHT
	putDHT("/probe/" + aProbe.getID() + "/name", aProbe.getName());
	putDHT("/probe/" + aProbe.getID() + "/datarate", aProbe.getDataRate().toString());
	putDHT("/probe/" + aProbe.getID() + "/on", aProbe.isOn());
	putDHT("/probe/" + aProbe.getID() + "/active", aProbe.isActive());

	// now probe attributes
	Collection<ProbeAttribute> attrs = aProbe.getAttributes();

	putDHT("/probeattribute/" + aProbe.getID() + "/size", attrs.size());
	// skip through all ProbeAttributes
	for (ProbeAttribute attr : attrs) {
	    addProbeAttribute(aProbe, attr);
	}

	return this;
    }

    /**
     * Add data for a ProbeAttribute.
     */
    public IMNode addProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
	String attrRoot = "/probeattribute/" + aProbe.getID() + "/" +
	    attr.getField() + "/";

	putDHT(attrRoot + "name", attr.getName());
	putDHT(attrRoot + "type", attr.getType().getCode());
	putDHT(attrRoot + "units", attr.getUnits());

	return this;

    }

    /*
     * Modify stuff
     */
    public IMNode modifyDataSource(DataSource ds) throws IOException {
	// remove then add
	throw new IOException("Not implemented yet!!");
    }

    public IMNode modifyProbe(Probe p) throws IOException {
	throw new IOException("Not implemented yet!!");
    }

    public IMNode modifyProbeAttribute(Probe p, ProbeAttribute pa)  throws IOException {
	throw new IOException("Not implemented yet!!");
    }


    /*
     * Remove stuff
     */
    public IMNode removeDataSource(DataSource ds) throws IOException {
	remDHT("/datasource/" + ds.getID() + "/name");

	Collection<Probe> probes = ds.getProbes();

	// skip through all probes
	for (Probe aProbe : probes) {
	    removeProbe(aProbe);
	}
	    
	return this;
    }

    public IMNode removeProbe(Probe aProbe) throws IOException {
	// add probe's ref to its data source
	// found through the ProbeManager
	DataSource ds = (DataSource)aProbe.getProbeManager();
	remDHT("/probe/" + aProbe.getID() + "/datasource");

	// add probe name to DHT
	remDHT("/probe/" + aProbe.getID() + "/name");
	remDHT("/probe/" + aProbe.getID() + "/datarate");
	remDHT("/probe/" + aProbe.getID() + "/on");
	remDHT("/probe/" + aProbe.getID() + "/active");

	// now probe attributes
	Collection<ProbeAttribute> attrs = aProbe.getAttributes();

	remDHT("/probeattribute/" + aProbe.getID() + "/size");
	// skip through all ProbeAttributes
	for (ProbeAttribute attr : attrs) {
	    removeProbeAttribute(aProbe, attr);
	}

	return this;
    }

    public IMNode removeProbeAttribute(Probe aProbe, ProbeAttribute attr)  throws IOException {
	String attrRoot = "/probeattribute/" + aProbe.getID() + "/" +
	    attr.getField() + "/";

	remDHT(attrRoot + "name");
	remDHT(attrRoot + "type");
	remDHT(attrRoot + "units");

	return this;
    }


    /**
     * Lookup DataSource info
     */
    public Object getDataSourceInfo(ID dsID, String info) {
	return getDHT("/datasource/" + dsID + "/" + info);
    }

    /**
     * Lookup probe details.
     */
    public Object getProbeInfo(ID probeID, String info) {
	return getDHT("/probe/" + probeID + "/" + info);
    }

    /**
     * Lookup probe attribute details.
     */
    public Object getProbeAttributeInfo(ID probeID, int field, String info) {
	return getDHT("/probeattribute/" + probeID + "/" + field + "/" + info);
    }




    /**
     * Put stuff into DHT.
     */
    public boolean putDHT(String aKey, Serializable aValue) {
	try {
	    BigInteger newKey = keyToBigInteger(aKey);
	    System.err.println("IMNode: put " + aKey + " K(" + newKey + ") => " + aValue);
	    dht.put(newKey, aValue);
	    return true;
	} catch (IOException ioe) {
	    System.err.println("IMNode: putDHT failed for key: '" + aKey + "' value: '" + aValue + "'");
	    ioe.printStackTrace();
	    return false;
	}
    }

    /**
     * Lookup info directly from the DHT.
     * @return the value if found, null otherwise
     */
    public Object getDHT(String aKey) {
	try {
	    BigInteger newKey = keyToBigInteger(aKey);
	    Object aValue = dht.get(newKey);
	    //System.err.println("IMNode: get " + aKey + " = " + newKey + " => " + aValue);
	    return aValue;
	} catch (IOException ioe) {
	    System.err.println("IMNode: getDHT failed for key: '" + aKey + "'");
	    ioe.printStackTrace();
	    return null;
	}
    }

    /**
     * Remove info from the DHT.
     * @return boolean
     */
    public boolean remDHT(String aKey) {
	try {
	    BigInteger newKey = keyToBigInteger(aKey);
	    dht.remove(newKey);
	    //System.err.println("IMNode: get " + aKey + " = " + newKey + " => " + aValue);
	    return true;
	} catch (IOException ioe) {
	    System.err.println("IMNode: remDHT failed for key: '" + aKey + "'");
	    ioe.printStackTrace();
	    return false;
	}
    }

    /**
     * Convert a key like /a/b/c/d into a fixed size big integer.
     */
    private BigInteger keyToBigInteger(String aKey) {
	// hash codes are signed ints
	int i = aKey.hashCode();
	// convert this into an unsigned long
	long l = 0xffffffffL & i;
	// create the BigInteger
	BigInteger result = BigInteger.valueOf(l);

	return result;
    }

}