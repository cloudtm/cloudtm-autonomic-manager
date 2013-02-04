// Measurement.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import java.util.List;

/**
 * An interface for measurements everywhere.
 * Has access to all info available in any situation.
 */
public interface Measurement {
    /**
     * Get the ID of the probe that created this measurement.
     */
    public ID getProbeID();

    /**
     * Get the service ID of the probe that created this measurement.
     */
    public ID getServiceID();

    /**
     * Get the group ID for this measurement.
     */
    public ID getGroupID();

    /**
     * Get the timestamp
     */
    public Timestamp getTimestamp();

    /**
     * Get the delta since the last measurement.
     */
    public Timestamp getDeltaTime();

    /**
     * Get the measurement type
     */
    public String getType();

    /**
     * Get the sequence number of this measurement.
     */
    public long getSequenceNo();

    /**
     * Get the attribute values
     */
    public List<ProbeValue> getValues();

}
