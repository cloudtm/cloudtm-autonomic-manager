// MulticastDataPlaneConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.distribution.multicast;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.MeasurementReporting;
import eu.reservoir.monitoring.core.MeasurementReceiver;
import eu.reservoir.monitoring.core.ConsumerMeasurement;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.*;
import eu.reservoir.monitoring.distribution.*;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * A MulticastDataPlaneConsumer is a DataPlane implementation
 * that receives Measurements by multicast.
 * @deprecated Choose either MulticastDataPlaneConsumerWithNames or MulticastDataPlaneConsumerNoNames
 */
public class MulticastDataPlaneConsumer extends MulticastDataPlaneConsumerWithNames implements DataPlane, MeasurementReporting, Receiving {
    /**
     * Construct a MulticastDataPlaneConsumer.
     */
    public MulticastDataPlaneConsumer(MulticastAddress addr) {
        super(addr);
    }

}