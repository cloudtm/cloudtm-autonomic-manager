// ProbeLifecycle.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core;

/**
 * The lifecycle of a Probe.
 */
public interface ProbeLifecycle extends Runnable {
    /**
     * Turn on a Probe
     */
    public ProbeLifecycle turnOnProbe();

    /**
     * Turn off a Probe
     */
    public ProbeLifecycle turnOffProbe();

    /**
     * Is this Probe turned on.
     * The thread is running, but is the Probe getting values.
     */
    public boolean isOn();

    /**
     * Activate the probe
     */
    public ProbeLifecycle activateProbe();

    /**
     * Deactivate the probe
     */
    public ProbeLifecycle deactivateProbe();

    /**
     * Has this probe been activated.
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isActive();

}
