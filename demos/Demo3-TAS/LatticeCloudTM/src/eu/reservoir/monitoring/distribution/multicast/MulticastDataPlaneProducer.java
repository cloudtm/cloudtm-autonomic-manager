// MulticastDataPlaneProducer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.distribution.multicast;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.DataSourceDelegate;
import eu.reservoir.monitoring.core.DataSourceDelegateInteracter;
import eu.reservoir.monitoring.core.ProbeMeasurement;
import eu.reservoir.monitoring.core.TypeException;
import eu.reservoir.monitoring.core.plane.*;
import eu.reservoir.monitoring.distribution.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A MulticastDataPlaneProducer is a DataPlane implementation
 * that sends Measurements by multicast.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 * @deprecated Choose either MulticastDataPlaneProducerWithNames or MulticastDataPlaneProducerNoNames
 */
public class MulticastDataPlaneProducer extends MulticastDataPlaneProducerWithNames implements DataPlane, DataSourceDelegateInteracter, Transmitting {
    /**
     * Construct a MulticastDataPlaneProducer.
     */
    public MulticastDataPlaneProducer(MulticastAddress addr) {
        super(addr);
    }


}