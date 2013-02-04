// ProbeMeasurement.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import java.util.List;

/**
 * An interface for measurements that are created by a probe.
 * It has the extra methods needed by the Probe and Data Source.
 * The producer has access to this data, the consumer is
 * remote and does not.
 */
public interface ProbeMeasurement extends Measurement {
    /**
     * Get the probe this Measurement is from
     */
    public Probe getProbe();

    /**
     * Add attribute / values to the measurement.
     */
    public Measurement addValues(List<ProbeValue> values);

    /**
     * Set the measurement type
     */
    public void setType(String t);

    /**
     * Set the serviceID.
     */
    public void setServiceID(ID sid);

    /**
     * Set the groupID.
     */
    public void setGroupID(ID gid);

    /**
     * Set the sequence number of this measurement.
     */
    public void setSequenceNo(long n);
}