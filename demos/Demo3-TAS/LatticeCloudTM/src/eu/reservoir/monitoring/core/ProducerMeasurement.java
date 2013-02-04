// ProducerMeasurement.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.Serializable;

/**
 * A class for measurements in producers.
 */
public class ProducerMeasurement implements Measurement, ProbeMeasurement {
    // the probe
    transient Probe probe;

    // the probe id
    ID id;

    // the service ID for the probe
    ID serviceID;

    // the group ID for the probe
    ID groupID;

    // the timestamp
    Timestamp timestamp = null;

    // the delta since the previous measurement
    Timestamp mDelta = null;

    // the type
    String type;

    // the sequence no
    long sequenceNo;

    // the attribute values list
    List<ProbeValue> attributes;


    /**
     * Construct a Measurement, given the Probe who sends the Measurement
     * and a list of ProbeValues.
     * The default type of the Measurement will be "Measurement".
     */
    public ProducerMeasurement(Probe aProbe, List<ProbeValue> attrs) {
	this(aProbe, attrs, "Measurement");
    }

    /**
     * Construct a Measurement, given the Probe who sends the Measurement
     * and a list of ProbeValues, and a type for the Measurement.
     */
    public ProducerMeasurement(Probe aProbe, List<ProbeValue> attrs, String theType) {
	timestamp = new Timestamp(System.currentTimeMillis());
	probe = aProbe;
	id = probe.getID();
	serviceID = probe.getServiceID();
	groupID = probe.getGroupID();

	mDelta = timestamp.minus(probe.getLastMeasurementCollection());
	type = theType;
	attributes = attrs;
    }

    /**
     * Construct a Measurement, given the Probe who sends the Measurement
     * and a map of field number to field value.
     * The default type of the Measurement will be "Measurement".
     * Throws a TypeException if any of the objects passed in are
     * not valid types for a Measurement.
     */
    public ProducerMeasurement(Probe aProbe, Map<Integer, Object> attrs) throws TypeException {
	this(aProbe, attrs, "Measurement");
    }

    /**
     * Construct a Measurement, given the Probe who sends the Measurement
     * and a map of field number to field value, and a type
     * for the Measurement.
     * Throws a TypeException if any of the objects passed in are
     * not valid types for a Measurement.
     */
    public ProducerMeasurement(Probe aProbe, Map<Integer, Object> attrs, String theType) throws TypeException {
	timestamp = new Timestamp(System.currentTimeMillis());
	probe = aProbe;
	id = probe.getID();
	serviceID = probe.getServiceID();
	
	mDelta = timestamp.minus(probe.getLastMeasurementCollection());
	type = theType;
	attributes = new ArrayList<ProbeValue>();

	// convert the map to a list of ProbeValue
	for (Map.Entry<Integer, Object> attr : attrs.entrySet()) {
	    attributes.add(new DefaultProbeValue(attr.getKey(), attr.getValue()));
	}
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
     * Get the probe this Measurement is from
     */
    public Probe getProbe() {
	return probe;
    }

    /**
     * Get the ID of the probe this Measurement is from
     */
    public ID getProbeID() {
	return id;
    }

    /**
     * Get the service ID of the probe this Measurement is from
     */
    public ID getServiceID() {
	return serviceID;
    }

    /**
     * Set the service ID.
     */
    public void setServiceID(ID sid) {
	serviceID = sid;
    }


    /**
     * Get the group ID for this Measurement
     */
    public ID getGroupID() {
	return groupID;
    }

    /**
     * Set the groupID.
     */
    public void setGroupID(ID gid) {
	groupID = gid;
    }

    /**
     * Get the measurement type
     */
    public String getType() {
	return type;
    }

    /**
     * Set the measurement type
     */
    public void setType(String t) {
	type = t;
    }

    /**
     * Get the sequence number of this measurement.
     */
    public long getSequenceNo() {
	return sequenceNo;
    }

    /**
     * Set the sequence number of this measurement.
     */
    public void setSequenceNo(long n) {
	sequenceNo = n;
    }

    /**
     * Get the attribute / values
     */
    public List<ProbeValue> getValues() {
	return attributes;
    }

    /**
     * Add attribute / values to the measurement.
     */
    public Measurement addValues(List<ProbeValue> values) {
	attributes.addAll(values);
	return this;
    }


    /**
     * To String
     */
    public String toString() {
	return ("seq: " + getSequenceNo() + "probeid: " + getProbeID() + " serviceid: " + getServiceID() + " groupid: " + getGroupID() + " timestamp: " +  timestamp + " delta: " + getDeltaTime() + 
		" ( probe: "  + probe + ") " + 
		" type: " + type + " attributes: " + attributes);
    }

}
