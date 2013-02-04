// HypervisorDataSource.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.appl.hypervisor;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.*;
import org.libvirt.*;

/**
 * A DataSource that dynamically adds and deletes a process list probe.
 */
public class HypervisorDataSource extends BasicDataSource implements DataSource {
    // the HypervisorCache
    HypervisorCache hypervisor;

    /*
     * Create a DataSource
     * It can dynamically add and delete a process list probe.
     */
    public HypervisorDataSource(String hostname) {
	setName(hostname + ".hypervisorDataSource");
    }

    /**
     * Set the connection to the hypervisor
     */
    public void setHypervisor(HypervisorCache hyp) {
	hypervisor = hyp;
    }

    /**
     *  Add a new probe for a vee.
     */
    public boolean addVEEProbe(int id, String vee, String currentHost) {
	System.out.println("NEW PROBE: " + vee);
	// try and get the probe
	Probe veeProbe = getProbeByName(vee);

	if (veeProbe == null) {
	    // it doesn't exists, so add it
	    Probe p = new HypervisorProbe(id, vee, currentHost, hypervisor);
	    addProbe(p);
	    activateProbe(p);
	    turnOnProbe(p);

	    System.out.println("Probe: " + p);
	    return true;
	} else {
	    // it does exist, so we've added this one before
	    return false;
	}
    }

    /**
     *  Delete a probe for a vee.
     */
    public boolean deleteVEEProbe(String vee) {
	System.out.println("DELETE PROBE: " + vee);
	// try and get the probe
	Probe veeProbe = getProbeByName(vee);

	if (veeProbe != null) {
	    // it exists, so remove it
	    System.out.println("HypervisorDataSource: before turnOffProbe " + vee);
	    turnOffProbe(veeProbe);
	    System.out.println("HypervisorDataSource: after turnOffProbe " + vee);
	    System.out.println("HypervisorDataSource: before deactivateProbe " + vee);
	    deactivateProbe(veeProbe);
	    System.out.println("HypervisorDataSource: after deactivateProbe " + vee);
	    System.out.println("HypervisorDataSource: before removeProbe " + vee);
	    removeProbe(veeProbe);
	    System.out.println("HypervisorDataSource: after removeProbe " + vee);

	    return true;
	} else {
	    // it doesn't exist
	    return false;
	}
    }

    

}
