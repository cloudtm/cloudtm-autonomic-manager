// MulticastDataPlaneConsumerWithNames.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

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
 * A MulticastDataPlaneConsumerWithNames is a DataPlane implementation
 * that receives Measurements by multicast.
 */
public class MulticastDataPlaneConsumerWithNames extends AbstractMulticastDataPlaneConsumer implements DataPlane, MeasurementReporting, Receiving {

    /**
     * Construct a MulticastDataPlaneConsumerWithNames.
     */
    public MulticastDataPlaneConsumerWithNames(MulticastAddress addr) {
        super(addr);
    }

    /**
     * This method is called just after a packet
     * has been received from some underlying transport
     * at a particular multicast address.
     * The expected message is XDR encoded and it's structure is:
     * +-------------------------------------------------------------------+
     * | data source id (long) | msg type (int) | seq no (int) | payload   |
     * +-------------------------------------------------------------------+
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws  IOException, TypeException {

	//System.out.println("DC: Received " + metaData);

	try {
	    DataInput dataIn = new XDRDataInputStream(bis);

	    //System.err.println("DC: datainputstream available = " + dataIn.available());

	    // get the DataSource id
	    ID dataSourceID = new ID(dataIn.readLong());

	    // get message type
	    int type = dataIn.readInt();

	    // convert int to MessageType object
	    MessageType mType = MessageType.lookup(type);

	    // check type
	    if (mType == null) {
		//System.err.println("type = " + type);
		return;
	    }

	    // get DataSource seq no
	    int seq = dataIn.readInt();

	    /*
	     * Check the DataSource seq no.
	     */
	    if (seqNoMap.containsKey(dataSourceID)) {
		// we've seen this DataSource before
		int prevSeqNo = seqNoMap.get(dataSourceID);

		if (seq == prevSeqNo + 1) {
		    // we got the correct message from that DataSource
		    // save this seqNo
		    seqNoMap.put(dataSourceID, seq);
		} else {
		    // a DataSource message is missing
		    // TODO: decide what to do
		    // currently: save this seqNo
		    seqNoMap.put(dataSourceID, seq);
		}
	    } else {
		// this is a new DataSource
		seqNoMap.put(dataSourceID, seq);
	    }

	    //System.err.println("Received " + type + ". mType " + mType + ". seq " + seq);

	    // Message meta data
	    MessageMetaData msgMetaData = new MessageMetaData(dataSourceID, seq, mType);

	    // read object and check it's type
	    switch (mType) {

	    case ANNOUNCE:
		System.err.println("ANNOUNCE not implemented yet!");
		break;

	    case MEASUREMENT:
		// decode the bytes into a measurement object
		MeasurementDecoder decoder = new MeasurementDecoderWithNames();
		Measurement measurement = decoder.decode(dataIn);

		if (measurement instanceof ConsumerMeasurementWithMetaData) {
		    // add the meta data into the Measurement
		    ((ConsumerMeasurementWithMetaData)measurement).setMessageMetaData(msgMetaData);
		    ((ConsumerMeasurementWithMetaData)measurement).setTransmissionMetaData(metaData);
		}

		
		//System.err.println("DC: datainputstream left = " + dataIn.available());
		// report the measurement
		report(measurement);
		//System.err.println("DC: m = " + measurement);
		break;
	    }


	} catch (IOException ioe) {
	    System.err.println("DataConsumer: failed to process measurement input. The Measurement data is likely to be bad.");
	    throw ioe;
	}
    }

}