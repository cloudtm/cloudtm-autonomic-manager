// AbstractDHTInfoPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.im.dht;

import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.DataSourceInteracter;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import java.io.Serializable;
import java.io.IOException;

/**
 * A DHTInfoPlane is an InfoPlane implementation
 * that sends the Information Model data.
 * It is also a DataSourceInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public abstract class AbstractDHTInfoPlane implements InfoPlane  {
    // An IMNode acts as a node in the DHT
    IMNode imNode;

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect() {
	return imNode.connect();
    }

    /**
     * Dicconnect from a delivery mechansim.
     */
    public boolean disconnect() {
	return imNode.disconnect();
    }

    // lookup some info in the InfoPlane
    public Object lookupDataSourceInfo(DataSource dataSource, String info) {
	return imNode.getDataSourceInfo(dataSource.getID(), info);
    }

    // lookup some info in the InfoPlane
    public Object lookupDataSourceInfo(ID dataSourceID, String info) {
	return imNode.getDataSourceInfo(dataSourceID, info);
    }

    // lookup some info in the InfoPlane
    public Object lookupProbeInfo(Probe probe, String info) {
	return imNode.getProbeInfo(probe.getID(), info);
    }

    // lookup some info in the InfoPlane
    public Object lookupProbeInfo(ID probeID, String info) {
	return imNode.getProbeInfo(probeID, info);
    }

    // lookup some info in the InfoPlane
    public Object lookupProbeAttributeInfo(Probe probe, int field, String info) {
	return imNode.getProbeAttributeInfo(probe.getID(), field, info);
    }

    // lookup some info in the InfoPlane
    public Object lookupProbeAttributeInfo(ID probeID, int field, String info) {
	return imNode.getProbeAttributeInfo(probeID, field, info);
    }

    /**
     * Put a value in the InfoPlane.
     */
    public boolean putInfo(String key, Serializable value) {
	return imNode.putDHT(key, value);
    }

    /**
     * Get a value from the InfoPlane.
     */
    public Object getInfo(String key) {
	return imNode.getDHT(key);
    }

    /**
     * Remove a value from the InfoPlane.
     */
    public boolean removeInfo(String key) {
	return imNode.remDHT(key);
    }
 
}