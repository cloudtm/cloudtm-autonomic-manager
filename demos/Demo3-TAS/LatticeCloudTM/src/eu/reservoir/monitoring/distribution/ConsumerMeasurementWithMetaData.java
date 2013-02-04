// ConsumerMeasurementWithMetaData.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.distribution;

import java.util.List;
import java.util.Map;
import java.io.Serializable;
import eu.reservoir.monitoring.core.*;

/**
 * A class for measurements in consumers extended to have meta data as well.
 */
public class ConsumerMeasurementWithMetaData extends ConsumerMeasurement implements Serializable {
    MetaData messageMetaData;
    MetaData transmissionMetaData;



    /**
     * Construct a Measurement.
     */
    public ConsumerMeasurementWithMetaData(long seqNo, ID pid, String theType, long ts, long delta, ID serviceID, ID groupID, List<ProbeValue> attrs) {
	super(seqNo, pid, theType, ts, delta, serviceID, groupID, attrs);
    }

    /**
     * Construct a Measurement
     */
    public ConsumerMeasurementWithMetaData(long seqNo, ID pid, String theType, long ts, long delta, ID serviceID, ID groupID, Map<Integer, Object> attrs) throws TypeException {
	super(seqNo, pid, theType, ts, delta, serviceID, groupID, attrs);	
    }

    /**
     * Get the message meta data
     */
    public MetaData getMessageMetaData() {
	return messageMetaData;
    }

    /**
     * Set the message meta data
     */
    public Measurement setMessageMetaData(MetaData md) {
	messageMetaData = md;
	return this;
    }

    /**
     * Get the transmission meta data
     */
    public MetaData getTransmissionMetaData() {
	return transmissionMetaData;
    }

    /**
     * Set the transmission meta data
     */
    public Measurement setTransmissionMetaData(MetaData md) {
	transmissionMetaData = md;
	return this;
    }
}
