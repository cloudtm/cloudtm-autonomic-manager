// MeasurementMessage.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core.plane;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.Probe;


/**
 * This implements the wrapping of a Measurement in the Data Plane.
 */
public class MeasurementMessage extends DataPlaneMessage {
    // The measurement
    ProbeMeasurement measurement;

    /**
     * Create a MeasurementMessage from a Measurement
     */
    public MeasurementMessage(ProbeMeasurement m) {
	measurement = m;
	type = MessageType.MEASUREMENT;
    }

    /**
     * Get the Measurement.
     */
    public ProbeMeasurement getMeasurement() {
	return measurement;
    }
}