package eu.reservoir.monitoring.distribution;

import java.util.List;

import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProbeValueWithName;

public class ConsumerMeasurementWithMetadataAndProbeName extends
		ConsumerMeasurementWithMetaData {
	public String probeName;

	public ConsumerMeasurementWithMetadataAndProbeName(long seqNo, ID pid,
			String theType, long ts, long delta, ID serviceID, ID groupID,
			List<ProbeValue> attrs, String probeName) {
		super(seqNo, pid, theType, ts, delta, serviceID, groupID, attrs);
		this.probeName = probeName;
	}

	public String getProbeName() {
		return probeName;
	}

    /**
     * To String
     */
    public String toString() {
	return ("seq: " + getSequenceNo() + " probename: " + getProbeName() + " probeid: " + getProbeID() + " serviceid: " + getServiceID() + " groupid: " + getGroupID() + " timestamp: " +  getTimestamp() + " delta: " + getDeltaTime() + " type: " + getType() + " attributes: " + getValues());
    }
	
}
