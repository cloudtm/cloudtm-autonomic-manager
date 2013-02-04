// UDPDataPlaneConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2009

package eu.reservoir.monitoring.distribution.udp;

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
import java.net.InetSocketAddress;

public class UDPDataPlaneConsumer extends UDPDataPlaneConsumerWithNames implements DataPlane, MeasurementReporting, Receiving {
    /**
     * Construct a UDPDataPlaneConsumer.
     */
    public UDPDataPlaneConsumer(InetSocketAddress addr) {
        super(addr);
    }

}