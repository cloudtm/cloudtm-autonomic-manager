// Probe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

/**
 * A probe
 */
public interface Probe extends ProbeInfo, ProbeLifecycle, ProbeReporting {
    /**
     * The status of a Probe.
     * It is either ON or OFF.
     */
    public enum Status {
	ON,				// the probe is on
        OFF;				// the probe is off
    }

    /**
     * This is how a probe collects data, either at a specified data rate
     * or by being notified with an event.
     */
    public enum CollectionType {
	AtDataRate,			// collect at a data rate
	OnEvent;			// collect after an event
    }

    /**
     * This is how a probe transmits data.  This can be: 
     * at a specified data rate
     * 
     */
    public enum ReportingType {
	AtDataRate,			// report at a data rate
	OnRead,				// only report data when it is read
	OnChange;			// report when data is changed
					// change is defined by the filter
    }

}
