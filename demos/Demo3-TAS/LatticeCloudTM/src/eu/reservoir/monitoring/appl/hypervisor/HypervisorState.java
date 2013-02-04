// HypervisorState.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.hypervisor;

import org.libvirt.*;

/**
 * This class to process the hypervisor state.
 */
public class HypervisorState {

    /**
     * Convert the VEE DomainState into a byte.
     */
    public static byte processState(DomainInfo.DomainState diState) {
	byte state;

	// Taken from the 'xm' manual page
	// The State field lists 6 states for a Virtual Domain, and which ones the current Domain is in. 
	// r - running    The domain is currently running on a CPU 
	// b - blocked    The domain is blocked, and not running or runnable. This can be caused because the
	//                domain is waiting on IO (a traditional wait state) or has gone to sleep because 
	//                there was nothing else for it to do. 
	// p - paused     The domain has been paused, usually occurring through the administrator running
	//                xm pause. When in a paused state the domain will still consume allocated resources
	//                like memory, but will not be eligible for scheduling by the hypervisor. 
	// s - shutdown   The guest has requested to be shutdown, rebooted or suspended, and the domain
	//                is in the process of being destroyed in response. 
	// c - crashed    The domain has crashed, which is always a violent ending. Usually this state can
	//                only occur if the domain has been configured not to restart on crash.
	// d - dying      The domain is in process of dying, but hasn't completely shutdown or crashed. 

	// Added n - nostate


	switch (diState) {
	case VIR_DOMAIN_BLOCKED:          // the domain is blocked on resource
	    state = 'b';
	    break;

	case VIR_DOMAIN_CRASHED:          // the domain is crashed
	    state = 'c';
	    break;

	case VIR_DOMAIN_NOSTATE:          // no state
	    state = 'n';
	    break;

	case VIR_DOMAIN_PAUSED:          // the domain is paused by user
	    state = 'p';
	    break;

	case VIR_DOMAIN_RUNNING:	     // the domain is running
	    state = 'r';
	    break;

	case VIR_DOMAIN_SHUTDOWN:        // the domain is being shut down
	    state = 's';
	    break;

	case VIR_DOMAIN_SHUTOFF:         // the domain is shut off
	    state = 'd';
	    break;

	default:
	    throw new RuntimeException("Domain.DomainInfo.state has illegal value for VEE " +  diState);
	}

	return state;
    }

}