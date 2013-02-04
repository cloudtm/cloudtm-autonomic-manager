// UserProcDataSource.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.*;
import java.io.IOException;

/**
 * A DataSource that dynamically adds and deletes a process list probe.
 */
public class UserProcDataSource extends BasicDataSource {
    /*
     * Create a DataSource
     * It can dynamically add and delete a process list probe.
     */
    public UserProcDataSource(String hostname) {
	setName(hostname + ".UserProcDataSource");
    }

    /**
     *  Add a new probe for a tty.
     */
    public boolean addTTYProbe(String tty) {
	System.out.println("NEW PROBE: " + tty);
	// try and get the probe
	Probe ttyProbe = getProbeByName(tty);

	if (ttyProbe == null) {
	    // it doesn't exists, so add it
	    Probe p = new UserProcTableProbe(tty);
	    addProbe(p);
	    turnOnProbe(p);

	    System.out.println("Probe: " + p);
	    return true;
	} else {
	    // it does exist, so we've added this one before
	    return false;
	}
    }

    /**
     *  Delete a probe for a tty.
     */
    public boolean deleteTTYProbe(String tty) {
	System.out.println("DELETE PROBE: " + tty);
	// try and get the probe
	Probe ttyProbe = getProbeByName(tty);

	if (ttyProbe != null) {
	    // it exists, so remove it
	    removeProbe(ttyProbe);
	    return true;
	} else {
	    // it doesn't exist
	    return false;
	}
    }

    

}
