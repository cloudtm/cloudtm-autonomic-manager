// DefaultDataSourceDelegate.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.core;

import eu.reservoir.monitoring.core.plane.*;
import java.io.Serializable;
import java.io.IOException;

/**
 * A DataSourceDelegate is a delegate for a DataSource
 * that sends interacts with the data plane, info plane, and
 * control plane.
 * It's role is to insulate the DataSource and the Probes
 * from the real implementations of the Planes.
 */
public class DefaultDataSourceDelegate extends AbstractPlaneInteracter implements DataSourceDelegate {
    /**
     * The DataSource this is delegating for.
     */
    DataSource dataSource;

    /**
     * Construct a DataSourceDelegate.
     */
    public DefaultDataSourceDelegate(DataSource ds) {
	dataSource = ds;
    }


    /**
     * Get the DataSource this is a delegate for.
     */
    public DataSource getDataSource() {
	return dataSource;
    }

    /**
     * Set the DataSource this is a delegate for.
     */
    public DataSource setDataSource(DataSource ds) {
	// dennounce the current DataSource
	DataSource old = null;

	// check if old DataSource was null
	// so we do not dennounce a null object
	if (dataSource != null) {
	    old = dataSource;
	    // and dennounce it
	    dennounce();
	}

	// set the DataSource
	dataSource = ds;

	// check if new DataSource is null
	// so we do not announce a null object
	if (dataSource != null) {
	    // and announce it
	    announce();
	}

	return old;
    }

    /**
     * Set the DataPlane this is a delegate for.
     */
    public PlaneInteracter setDataPlane(DataPlane dataPlane) {
	// set dataPlane
	this.dataPlane = dataPlane;

	if (dataPlane != null) {
	    // bind the DataPlane to the DataSource
	    if (dataPlane instanceof DataSourceDelegateInteracter) {
		((DataSourceDelegateInteracter)dataPlane).setDataSourceDelegate(this);
	    }
	}

	return this;
    }

    /**
     * Set the ControlPlane this is a delegate for.
     */
    public PlaneInteracter setControlPlane(ControlPlane controlPlane) {
	// set controlPlane
	this.controlPlane = controlPlane;


	if (controlPlane != null) {
	    // bind the ControlPlane to the DataSource
	    if (controlPlane instanceof DataSourceDelegateInteracter) {
		((DataSourceDelegateInteracter)controlPlane).setDataSourceDelegate(this);
	    }
	}

	return this;
    }

    /**
     * Set the InfoPlane this is a delegate for.
     */
    public PlaneInteracter setInfoPlane(InfoPlane infoPlane) {
	// set infoPlane
	this.infoPlane = infoPlane;


	if (infoPlane != null) {
	    // bind the InfoPlane to the DataSource
	    if (infoPlane instanceof DataSourceDelegateInteracter) {
		((DataSourceDelegateInteracter)infoPlane).setDataSourceDelegate(this);
	    }
	}

	return this;
    }

    /*
     * Data Service
     */
    /**
     * Send a message.
     * @return -1 if something goes wrong
     * @return 0 if there is no delegate or no data plane
     */
    public int sendData(DataPlaneMessage dpm) throws Exception {
	if (dataPlane != null) {
	    return dataPlane.sendData(dpm);
	} else {
	    return 0;
	}
    }

    /**
     * This method is called just after a message
     * has been sent to the underlying transport.
     */
    public boolean sentData(int id) {
	// What to do.
	return true;
    }

    /*
     * Info Service
     */

    // Look up things.
    
    /**
     * Lookup some DataSource info in the InfoPlane.
     */
    public Object lookupDataSourceInfo(DataSource dataSource, String info) {
	if (infoPlane != null) {
	    return infoPlane.lookupDataSourceInfo(dataSource, info);
	} else {
	    return null;
	}
    }

    /**
     * Lookup some DataSource info in the InfoPlane.
     * Mostly used at the management end, as it uses DataSource ID.
     */
    public Object lookupDataSourceInfo(ID dataSourceID, String info) {
	if (infoPlane != null) {
	    return infoPlane.lookupDataSourceInfo(dataSourceID, info);
	} else {
	    return null;
	}
    }

    /**
     * Lookup some Probe info in the InfoPlane.
     */
    public Object lookupProbeInfo(Probe probe, String info) {
	if (infoPlane != null) {
	    return infoPlane.lookupProbeInfo(probe, info);
	} else {
	    return null;
	}
    }

    /**
     * Lookup some Probe info in the InfoPlane.
     * Mostly used at the management end, as it uses Probe ID.
     */
    public Object lookupProbeInfo(ID probeID, String info) {
	if (infoPlane != null) {
	    return infoPlane.lookupProbeInfo(probeID, info);
	} else {
	    return null;
	}
    }


    /**
     * Lookup some ProbeAttribute info in the InfoPlane.
     */
    public Object lookupProbeAttributeInfo(Probe probe, int field, String info) {
	if (infoPlane != null) {
	    return infoPlane.lookupProbeAttributeInfo(probe, field, info);
	} else {
	    return null;
	}
    }


    /**
     * Lookup some ProbeAttribute info in the InfoPlane.
     * Mostly used at the management end, as it uses Probe ID.
     */
    public Object lookupProbeAttributeInfo(ID probeID, int field, String info) {
	if (infoPlane != null) {
	    return infoPlane.lookupProbeAttributeInfo(probeID, field, info);
	} else {
	    return null;
	}
    }

    // Add things

    /**
     * Add DataSource info to the InfoPlane.
     */
    public boolean addDataSourceInfo(DataSource ds) {
	if (infoPlane != null) {
	    return infoPlane.addDataSourceInfo(ds);
	} else {
	    return false;
	}
    }

    /**
     * Add Probe info to the InfoPlane.
     */
    public boolean addProbeInfo(Probe p) {
    	System.out.println("INFOPLANEE "+infoPlane);
	if (infoPlane != null) {
	    return infoPlane.addProbeInfo(p);
	} else {
	    return false;
	}
    }

    /**
     * Add ProbeAttribute info to the InfoPlane.
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	if (infoPlane != null) {
	    return infoPlane.addProbeAttributeInfo(p, pa);
	} else {
	    return false;
	}
    }

    // Modify things

    /**
     * Modify DataSource Info.
     */
    public boolean modifyDataSourceInfo(DataSource ds) {
	if (infoPlane != null) {
	    return infoPlane.modifyDataSourceInfo(ds);
	} else {
	    return false;
	}
    }

    /**
     * Modify Probe Info.
     */
    public boolean modifyProbeInfo(Probe p) {
	if (infoPlane != null) {
	    return infoPlane.modifyProbeInfo(p);
	} else {
	    return false;
	}
    }

    /**
     * Modify  ProbeAttribute Info for a Probe.
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	if (infoPlane != null) {
	    return infoPlane.modifyProbeAttributeInfo(p, pa);
	} else {
	    return false;
	}
    } 

    // Remove things

    /**
     * Remove  DataSource info from the InfoPlane.
     */
    public boolean removeDataSourceInfo(DataSource ds) {
	if (infoPlane != null) {
	    return infoPlane.removeDataSourceInfo(ds);
	} else {
	    return false;
	}
    }

    /**
     * Remove Probe info from the InfoPlane.
     */
    public boolean removeProbeInfo(Probe p) {
	if (infoPlane != null) {
	    return infoPlane.removeProbeInfo(p);
	} else {
	    return false;
	}
    }

    /**
     * Remove ProbeAttribute info from the InfoPlane.
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa) {
	if (infoPlane != null) {
	    return infoPlane.removeProbeAttributeInfo(p, pa);
	} else {
	    return false;
	}
    }

    /**
     * Put a value in the InfoPlane.
     */
    public boolean putInfo(String key, Serializable value) {
	if (infoPlane != null) {
	    return infoPlane.putInfo(key, value);
	} else {
	    return false;
	}
    }

    /**
     * Get a value from the InfoPlane.
     */
    public Object getInfo(String key) {
	if (infoPlane != null) {
	    return infoPlane.getInfo(key);
	} else {
	    return null;
	}
    }

    /**
     * Remove a value from the InfoPlane.
     */
    public boolean removeInfo(String key) {
	if (infoPlane != null) {
	    return infoPlane.removeInfo(key);
	} else {
	    return false;
	}
    }

    /*
     * Control Service
     */

    // Probe methods

    /*
     * The are methods that are also in the ProbeInfo
     * interface in the 'core' package.
     */

    /**
     * Get the name of the Probe
     */
    public String getProbeName(ID probeID) {
	return dataSource.getProbeName(probeID);
    }

    /**
     * Set the name of the Probe
     */
    public boolean setProbeName(ID probeID, String name) {
	return dataSource.setProbeName(probeID, name);
    }	

    /**
     * Get the Service ID of the Probe.
     */
    public ID getProbeServiceID(ID probeID) {
	return dataSource.getProbeServiceID(probeID);
    }

    /**
     * Set the Service ID for a Probe
     */
    public boolean setProbeServiceID(ID probeID, ID id) {
	return dataSource.setProbeServiceID(probeID, id);
    }

    /**
     * Get the Group ID of the Probe.
     */
    public ID getProbeGroupID(ID probeID) {
	return dataSource.getProbeGroupID(probeID);
    }

    /**
     * Set the Group ID for a Probe
     */
    public boolean setProbeGroupID(ID probeID, ID id) {
	return dataSource.setProbeGroupID(probeID, id);
    }


    /**
     * Get the data rate for a Probe 
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getProbeDataRate(ID probeID) {
	return dataSource.getProbeDataRate(probeID);
    }

    /**
     * Set the data rate for a Probe
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public boolean setProbeDataRate(ID probeID, Rational dataRate) {
	dataSource.setProbeDataRate(probeID, dataRate);
	return true;
    }

    /**
     * Get the last measurement that was collected.
     */
    public Measurement getProbeLastMeasurement(ID probeID) {
	return dataSource.getProbeLastMeasurement(probeID);
    }

    /**
     * Get the last time a measurement was collected.
     */
    public Timestamp getProbeLastMeasurementCollection(ID probeID) {
	return dataSource.getProbeLastMeasurementCollection(probeID);
    }

    /*
     * The are methods that are also in the ProbeLifecycle
     * interface in the 'core' package.
     */

    /**
     * Turn on a Probe
     */
    public boolean turnOnProbe(ID probeID) {
	dataSource.turnOnProbe(probeID);
	return true;
    }

    /**
     * Turn off a Probe
     */
    public boolean turnOffProbe(ID probeID) {
	dataSource.turnOffProbe(probeID);
	return true;
    }

    /**
     * Is this Probe turned on.
     * The thread is running, but is the Probe getting values.
     */
    public boolean isProbeOn(ID probeID) {
	return dataSource.isProbeOn(probeID);
    }

    /**
     * Activate the probe
     */
    public boolean activateProbe(ID probeID) {
	dataSource.activateProbe(probeID);
	return true;
    }

    /**
     * Deactivate the probe
     */
    public boolean deactivateProbe(ID probeID) {
	dataSource.deactivateProbe(probeID);
	return true;
    }

    /**
     * Has this probe been activated.
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isProbeActive(ID probeID) {
	return dataSource.isProbeActive(probeID);
    }


    // Data Source methods

    /**
     * Get the name of the DataSource
     */
    public String getDataSourceName() {
	return dataSource.getName();
    }

    /**
     * Set the name of the DataSource
     */
    public boolean setDataSourceName(String name) {
	dataSource.setName(name);
	return true;
    }


}