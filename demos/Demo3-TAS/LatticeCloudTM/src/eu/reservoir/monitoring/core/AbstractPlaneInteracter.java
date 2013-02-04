// AbstractPlaneInteracter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.core;

import eu.reservoir.monitoring.core.plane.*;
import java.io.IOException;

/**
 * An object that interacts with the data plane, the control plane,
 * and the info plane.
 */
public abstract class AbstractPlaneInteracter implements PlaneInteracter {
    /**
     * The transmitter for the data domain.
     */
    DataPlane dataPlane;

    PlaneConnectStatus dataPlaneStatus = PlaneConnectStatus.DISCONNECTED;

    /**
     * The Interacter for the control domain.
     * This gets control messages and sends responses.
     */
    ControlPlane controlPlane;

    PlaneConnectStatus controlPlaneStatus = PlaneConnectStatus.DISCONNECTED;

    /**
     * The Information plane holds info on the probes and data sources.
     */
    InfoPlane infoPlane;

    PlaneConnectStatus infoPlaneStatus = PlaneConnectStatus.DISCONNECTED;

    /**
     * Get the DataPlane this is a delegate for.
     */
    public DataPlane getDataPlane() {
	return dataPlane;
    }

    /**
     * Set the DataPlane this is a delegate for.
     */
    public PlaneInteracter setDataPlane(DataPlane dataPlane) {
	// set dataPlane
	this.dataPlane = dataPlane;

	return this;
    }

    /**
     * Get the ControlPlane this is a delegate for.
     */
    public ControlPlane getControlPlane() {
	return controlPlane;
    }

    /**
     * Set the ControlPlane this is a delegate for.
     */
    public PlaneInteracter setControlPlane(ControlPlane controlPlane) {
	// set controlPlane
	this.controlPlane = controlPlane;

	return this;
    }

    /**
     * Get the InfoPlane this is a delegate for.
     */
    public InfoPlane getInfoPlane() {
	return infoPlane;
    }

    /**
     * Set the InfoPlane this is a delegate for.
     */
    public PlaneInteracter setInfoPlane(InfoPlane infoPlane) {
	// set infoPlane
	this.infoPlane = infoPlane;
	return this;
    }


    /**
     * Connect to the delivery mechanisms.
     * This tries to activate connections for: the data plane,
     * the control plane, and the info plane.
     */
    public boolean connect() {
	boolean failed = false;
	boolean conn = false;

	// connect to data plane
	if (dataPlane != null) {
	    dataPlaneStatus = PlaneConnectStatus.CONNECTING;

	    conn = dataPlane.connect();

	    if (!conn) { 
		failed = true; 
		dataPlaneStatus = PlaneConnectStatus.FAILED;
	    } else {
		dataPlaneStatus = PlaneConnectStatus.CONNECTED;
	    }
	}

	// connect to control plane
	if (controlPlane != null) {
	    controlPlaneStatus = PlaneConnectStatus.CONNECTING;

	    conn = controlPlane.connect();

	    if (!conn) { 
		failed = true; 
		controlPlaneStatus = PlaneConnectStatus.FAILED;
	    } else {
		controlPlaneStatus = PlaneConnectStatus.CONNECTED;
	    }
	}

	// connect to info plane
	if (infoPlane != null) {
	    infoPlaneStatus = PlaneConnectStatus.CONNECTING;

	    conn = infoPlane.connect();

	    if (!conn) {
		failed = true;
		infoPlaneStatus = PlaneConnectStatus.FAILED;
	    } else {
		infoPlaneStatus = PlaneConnectStatus.CONNECTED;
	    }
	}

	
	if (failed) {
	    // failed to connect
	    return false;
	} else {
	    // try and announce
	    boolean ann = announce();

	    if (ann) {
		// we connected and announced
		return true;
	    } else {
		// we connected but could not announce
		return false;
	    }
	}

    }


    /**
     * Is the PlaneInteracter connected to a delivery mechansim.
     */
    public boolean isConnected() {
	// if there is a mix of CONNECTED and DISCONNECTED
	// then that is OK.  The DISCONNECTED ones
	// were never asked for.
	// if any of the PlaneConnectStatus has a value of
	// CONNECTING or FAILED then somethnig is not right
	if ((dataPlaneStatus.equals(PlaneConnectStatus.CONNECTED) ||
	     dataPlaneStatus.equals(PlaneConnectStatus.DISCONNECTED) ) &&
	    (controlPlaneStatus.equals(PlaneConnectStatus.CONNECTED) ||
	     controlPlaneStatus.equals(PlaneConnectStatus.DISCONNECTED) ) &&
	    (infoPlaneStatus.equals(PlaneConnectStatus.CONNECTED) ||
	     infoPlaneStatus.equals(PlaneConnectStatus.DISCONNECTED) ) ) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Disconnect from the delivery mechanisms.
     * This tries to deactivate connections for: the data plane,
     * the control plane, and the info plane.
     */
    public boolean disconnect() {
	boolean failed = true;
	boolean conn = false;

	// de-announce the Data Source
	dennounce();

	/*
	 * now try the disconnects.
	 */

	// connect to data plane
	if (dataPlane != null) {
	    dataPlaneStatus = PlaneConnectStatus.DISCONNECTING;

	    conn = dataPlane.disconnect();

	    if (!conn) { 
		failed = true; 
		dataPlaneStatus = PlaneConnectStatus.FAILED;
	    } else {
		dataPlaneStatus = PlaneConnectStatus.DISCONNECTED;
	    }
	}

	// connect to control plane
	if (controlPlane != null) {
	    controlPlaneStatus = PlaneConnectStatus.DISCONNECTING;

	    conn = controlPlane.disconnect();

	    if (!conn) { 
		failed = true;
		controlPlaneStatus = PlaneConnectStatus.FAILED;
	    } else {
		controlPlaneStatus = PlaneConnectStatus.DISCONNECTED;
	    }
	}

	// connect to info plane
	boolean i = false;

	if (infoPlane != null) {
	    infoPlaneStatus = PlaneConnectStatus.DISCONNECTING;

	    conn = infoPlane.disconnect();

	    if (!conn) { 
		failed = true; 
		infoPlaneStatus = PlaneConnectStatus.FAILED;
	    } else {
		infoPlaneStatus = PlaneConnectStatus.DISCONNECTED;
	    }
	}

	if (failed) {
	    // failed to disconnect
	    return false;
	} else {
	    return true;
	}
    }

    /**
     * Announce that the PlaneInteracter is up and running
     */
    public boolean announce() {
	try {
	    // announce the DataSource to the dataPlane
	    if (dataPlane != null) {
		dataPlane.announce();
	    }

	    // announce the DataSource to the controlPlane
	    if (controlPlane != null) {
		controlPlane.announce();
	    }

	    // announce the DataSource to the infoPlane
	    if (infoPlane != null) {
		infoPlane.announce();
	    }

	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * Un-announce that the PlaneInteracter is up and running
     */
    public boolean dennounce() {
	try {
	    // announce the DataSource to the dataPlane
	    if (dataPlane != null) {
		dataPlane.dennounce();
	    }

	    // announce the DataSource to the controlPlane
	    if (controlPlane != null) {
		controlPlane.dennounce();
	    }

	    // announce the DataSource to the infoPlane
	    if (infoPlane != null) {
		infoPlane.dennounce();
	    }

	    return true;
	} catch (Exception e) {
	    return false;
	}
    }


    /**
     * The connection status of a Plane.
     */
    enum PlaneConnectStatus {
	CONNECTED, DISCONNECTED, CONNECTING, DISCONNECTING, FAILED;
    }
}