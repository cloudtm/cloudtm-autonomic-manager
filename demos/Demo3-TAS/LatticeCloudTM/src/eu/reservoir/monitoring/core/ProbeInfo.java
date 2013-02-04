// ProbeInfo.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core;

import java.util.Map;
import java.util.Collection;

/**
 * A probe
 */
public interface ProbeInfo {
    /*
     * The first bunch are also used from the Control Service.
     */

    /**
     * Get the ID of the Probe.
     */
    public ID getID();

    /**
     * Set the Probe ID
     */
    public Probe setID(ID id);

    /**
     * Get the name of the Probe
     */
    public String getName();

    /**
     * Set the name of the Probe
     */
    public Probe setName(String name);

    /**
     * Get the data rate for a Probe
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getDataRate();

    /**
     * Set the data rate for a Probe
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Probe setDataRate(Rational dataRate);

    /**
     * Get the Service ID of the Probe.
     */
    public ID getServiceID();

    /**
     * Set the Service ID for a Probe
     */
    public Probe setServiceID(ID sid);

    /**
     * Get the Group ID of the Probe.
     */
    public ID getGroupID();

    /**
     * Set the Group ID for a Probe
     */
    public Probe setGroupID(ID gid);

    /**
     * Get the last measurement that was collected.
     */
    public Measurement getLastMeasurement();

    /**
     * Get the last time a measurement was collected.
     */
    public Timestamp getLastMeasurementCollection();

    /*
     * Other methods.
     */

    /**
     * Define an element of the data dictionary
     */
    public Probe addProbeAttribute(ProbeAttribute attribute);

    /**
     * Get the Probe's Attributes.
     */
    public Collection<ProbeAttribute> getAttributes();
      
    /**
     * Get the ProbeAttribute with field no. N.
     */
    public ProbeAttribute getAttribute(int n);

    /**
     * Get the meta data for a probe.
     * Returns a  map of probe attributes,
     * e.g  [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String, Object> getMetaData();

    /**
     * Get the manager of a Probe.
     */
    public ProbeManager getProbeManager();

    /**
     * Set the manager of a Probe.
     */
    public ProbeLifecycle setProbeManager(ProbeManager pm);

}
