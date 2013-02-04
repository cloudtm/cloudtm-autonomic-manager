// MulticastDataPlaneProducerNoNames.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

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
 * A MulticastDataPlaneProducerNoNames is a DataPlane implementation
 * that sends Measurements by multicast.
 * It is also a DataSourceDelegateInteracter so it can, if needed,
 * talk to the DataSource object it gets bound to.
 */
public class MulticastDataPlaneProducerNoNames extends AbstractMulticastDataPlaneProducer implements DataPlane, DataSourceDelegateInteracter, Transmitting {

    /**
     * Construct a MulticastDataPlaneProducerNoNames.
     */
    public MulticastDataPlaneProducerNoNames(MulticastAddress addr) {
        super(addr);
    }

    /**
     * Send a message onto the multicast address.
     * The message is XDR encoded and it's structure is:
     * +-------------------------------------------------------------------+
     * | data source id (long) | msg type (int) | seq no (int) | payload   |
     * +-------------------------------------------------------------------+
     */
    public int transmit(DataPlaneMessage dpm) throws Exception { // IOException, TypeException {
	// convert DataPlaneMessage into a ByteArrayOutputStream
	// then transmit it

	try {
	    // convert the object to a byte []
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	    DataOutput dataOutput = new XDRDataOutputStream(byteStream);

	    // write the DataSource id
	    dataOutput.writeLong(dpm.getDataSource().getID().longValue());

	    // write message type
	    dataOutput.writeInt(dpm.getType().getValue());

	    // write DataSource seqNo
	    int seqNo = dpm.getSeqNo();
	    dataOutput.writeInt(seqNo);

	    // write the message object
	    switch (dpm.getType()) {

	    case ANNOUNCE:
		System.err.println("ANNOUNCE not implemented yet!");
		break;

	    case MEASUREMENT:
		// extract Measurement from message object
		ProbeMeasurement measurement = ((MeasurementMessage)dpm).getMeasurement();
		// encode the measurement, ready for transmission
		MeasurementEncoder encoder = new MeasurementEncoder(measurement);
		encoder.encode(dataOutput);

		break;
	    }

	    //System.err.println("DP: " + dpm + " AS " + byteStream);

	    // now tell the multicaster to transmit this byteStream
	    mcastTransmitter.transmit(byteStream, seqNo);

	    return 1;
	} catch (TypeException te) {
	    te.printStackTrace(System.err);
	    return 0;
	}
    }
}