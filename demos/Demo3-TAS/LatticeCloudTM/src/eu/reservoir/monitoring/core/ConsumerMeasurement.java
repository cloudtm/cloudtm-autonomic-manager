// ConsumerMeasurement.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * A class for measurements in consumers.
 */
public class ConsumerMeasurement implements Measurement, Serializable {
    // the probe id
    ID probeID;

    // the service ID for the probe
    ID serviceID;

    // the group ID 
    ID groupID;

    // the timestamp
    Timestamp timestamp = null;

    // the measurement time delta
    Timestamp mDelta = null;

    // the type
    String type;

    // the sequence no
    long sequenceNo;

    // the attribute values list
    List<ProbeValue> attributes;

    /**
     * Construct a ConsumerMeasurement.
     */
    public ConsumerMeasurement(long seqNo, ID pid, String theType, long ts, long delta, ID serviceID, ID groupID, List<ProbeValue> attrs) {
	sequenceNo = seqNo;
	probeID = pid;
	timestamp = new Timestamp(ts);
	mDelta = new Timestamp(delta);
	type = theType;
	this.serviceID = serviceID;
	this.groupID = groupID;

	attributes = attrs;
    }

    /**
     * Construct a Measurement
     * Throws a TypeException if any of the objects passed in are
     * not valid types for a Measurement.
     */
    public ConsumerMeasurement(long seqNo, ID pid, String theType, long ts, long delta, ID serviceID, ID groupID, Map<Integer, Object> attrs) throws TypeException {
	sequenceNo = seqNo;
	probeID = pid;
	timestamp = new Timestamp(ts);
	mDelta = new Timestamp(delta);
	type = theType;
	this.serviceID = serviceID;
	this.groupID = groupID;

	// convert the map to a list of ProbeValue
	for (Map.Entry<Integer, Object> attr : attrs.entrySet()) {
	    attributes.add(new DefaultProbeValue(attr.getKey(), attr.getValue()));
	}
    }

    /**
     * Get the ID of the probe that created this measurement.
     */
    public ID getProbeID() {
	return probeID;
    }

    /**
     * Get the service ID of the probe this Measurement is from
     */
    public ID getServiceID() {
	return serviceID;
    }

    /**
     * Get the group ID for this Measurement 
     */
    public ID getGroupID() {
	return groupID;
    }

    /**
     * Get the timestamp
     */
    public Timestamp getTimestamp() {
	return timestamp;
    }

    /**
     * Get the delta since the last measurement.
     */
    public Timestamp getDeltaTime() {
	return mDelta;
    }

   /**
     * Get the measurement type
     */
    public String getType() {
	return type;
    }

    /**
     * Get the sequence number of this measurement.
     */
    public long getSequenceNo() {
	return sequenceNo;
    }

    /**
     * Get the attribute / values
     */
    public List<ProbeValue> getValues() {
	return attributes;
    }


    /**
     * To String
     */
    public String toString() {
	return ("seq: " + getSequenceNo() + " probeid: " + getProbeID() + " serviceid: " + getServiceID() + " groupid: " + getGroupID() + " timestamp: " +  timestamp + " delta: " + mDelta + " type: " + type + " attributes: " + attributes);
    }

}
