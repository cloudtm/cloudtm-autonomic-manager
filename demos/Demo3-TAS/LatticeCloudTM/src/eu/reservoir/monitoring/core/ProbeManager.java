// ProbeManager.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core;


/**
 * An interface for managing probes
 */
public interface ProbeManager {
    /*
     * Lifecycle stuff
     */

    /**
     * Turn on a Probe by name
     */
    public DataSource turnOnProbe(String name);

    /**
     * Turn on a Probe by ID
     */
    public DataSource turnOnProbe(ID probeID);

    /**
     * Turn on a Probe
     */
    public DataSource turnOnProbe(Probe p);

    /**
     * Turn off a Probe by name
     */
    public DataSource turnOffProbe(String name);

    /**
     * Turn off a Probe by ID
     */
    public DataSource turnOffProbe(ID probeID);

    /**
     * Turn off a Probe
     */
    public DataSource turnOffProbe(Probe p);

    /**
     * Is this Probe turned on by name
     * The thread is running, but is the Probe getting values.
     */
    public boolean isProbeOn(String name);

    /**
     * Is this Probe turned on by ID
     * The thread is running, but is the Probe getting values.
     */
    public boolean isProbeOn(ID probeID);

    /**
     * Is this Probe turned on.
     * The thread is running, but is the Probe getting values.
     */
    public boolean isProbeOn(Probe p);

    /**
     * Activate a Probe by name
     */
    public DataSource activateProbe(String name);

    /**
     * Activate a Probe by ID
     */
    public DataSource activateProbe(ID probeID);

    /**
     * Activate a Probe
     */
    public DataSource activateProbe(Probe p);

    /**
     * Deactivate a Probe by name
     */
    public DataSource deactivateProbe(String name);

    /**
     * Deactivate a Probe by ID
     */
    public DataSource deactivateProbe(ID probeID);

    /**
     * Deactivate a Probe
     */
    public DataSource deactivateProbe(Probe p);

    /**
     * Has this probe been activated by name
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isProbeActive(String name);


    /**
     * Has this probe been activated by ID
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isProbeActive(ID probeID);


    /**
     * Has this probe been activated.
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isProbeActive(Probe p);


    /*
     * Probe interaction stuff
     */

    /**
     * Get the name of the Probe by name
     */
    public String getProbeName(String name);

    /**
     * Get the name of the Probe by ID
     */
    public String getProbeName(ID probeID);

    /**
     * Get the name of the Probe
     */
    public String getProbeName(Probe p);

    /**
     * Set the name of the Probe by name
     */
    public boolean setProbeName(String name, String newName);

    /**
     * Set the name of the Probe by ID
     */
    public boolean setProbeName(ID probeID, String newName);

    /**
     * Set the name of the Probe
     */
    public boolean setProbeName(Probe p, String newName);

    /**
     * Get the Service ID of the Probe by name
     */
    public ID getProbeServiceID(String name);

    /**
     * Get the Service ID of the Probe by ID
     */
    public ID getProbeServiceID(ID probeID);

    /**
     * Get the Service ID of the Probe.
     */
    public ID getProbeServiceID(Probe p);

    /**
     * Set the Service ID for a Probe by name
     */
    public boolean setProbeServiceID(String name, ID id);

    /**
     * Set the Service ID for a Probe by ID
     */
    public boolean setProbeServiceID(ID probeID, ID id);

    /**
     * Set the Service ID for a Probe
     */
    public boolean setProbeServiceID(Probe p, ID id);

    /**
     * Get the Group ID of the Probe by name
     */
    public ID getProbeGroupID(String name);

    /**
     * Get the Group ID of the Probe by ID
     */
    public ID getProbeGroupID(ID probeID);

    /**
     * Get the Group ID of the Probe.
     */
    public ID getProbeGroupID(Probe p);

    /**
     * Set the Group ID for a Probe by name
     */
    public boolean setProbeGroupID(String name, ID id);

    /**
     * Set the Group ID for a Probe by ID
     */
    public boolean setProbeGroupID(ID probeID, ID id);

    /**
     * Set the Group ID for a Probe
     */
    public boolean setProbeGroupID(Probe p, ID id);

    /**
     * Get the data rate for a Probe by name
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getProbeDataRate(String name);

    /**
     * Get the data rate for a Probe by ID
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getProbeDataRate(ID probeID);

    /**
     * Get the data rate for a Probe 
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getProbeDataRate(Probe p);

    /**
     * Set the data rate for a Probe by name
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public DataSource setProbeDataRate(String name, Rational dataRate);

    /**
     * Set the data rate for a Probe by ID
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public DataSource setProbeDataRate(ID probeID, Rational dataRate);

    /**
     * Set the data rate for a Probe
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public DataSource setProbeDataRate(Probe p, Rational dataRate);

    /**
     * Get the last measurement that was collected by name
     */
    public Measurement getProbeLastMeasurement(String name);

    /**
     * Get the last measurement that was collected by ID
     */
    public Measurement getProbeLastMeasurement(ID probeID);

    /**
     * Get the last measurement that was collected.
     */
    public Measurement getProbeLastMeasurement(Probe p);

    /**
     * Get the last time a measurement was collected by name.
     */
    public Timestamp getProbeLastMeasurementCollection(String name);

    /**
     * Get the last time a measurement was collected by ID.
     */
    public Timestamp getProbeLastMeasurementCollection(ID probeID);

    /**
     * Get the last time a measurement was collected.
     */
    public Timestamp getProbeLastMeasurementCollection(Probe p);


    /*
     * Probe  -> ProbeManager notification
     */

    /**
     * Receiver of a measurement from a Probe.
     */
    public int notifyMeasurement(Measurement m);


}
