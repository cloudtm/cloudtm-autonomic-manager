// MeasurementPrinter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.core.plane.InfoPlane;
import eu.reservoir.monitoring.distribution.*;

import java.util.List;

/**
 * A class that prints Measurements.
 * It is a Reporter that can be passed into a Consumer.
 */
public class MeasurementPrinter implements Reporter {
    // The info plane which contains the info model
    InfoPlane infoModel;

    /**
     * The MeasurementPrinter attaches to the InfoPlane
     * to get meta data about a Measurement.
     */
    public MeasurementPrinter(InfoPlane infoPlane) {
	infoModel = infoPlane;
    }

    /**
     * Receiver of a measurment.
     */
    public void report(Measurement m) {
	// get some meta data
	//MetaData msgMetaData = ((ConsumerMeasurementWithMetaData)m).getMessageMetaData();

	//System.out.print("seqNo: " + ((MessageMetaData)msgMetaData).seqNo + " ");

	String probeName = (String)infoModel.lookupProbeInfo(m.getProbeID(), "name");

	System.out.print(probeName + " => ");

	System.out.print(" seqno: " + m.getSequenceNo());
	System.out.print(" timestamp: " + m.getTimestamp());
	System.out.print(" time delta: " + m.getDeltaTime());
	System.out.print(" type: " + m.getType() + ". ");

	List<ProbeValue> values = m.getValues();

	for (ProbeValue aValue : values) {
	    String name = (String)infoModel.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "name");
	    Byte type = (Byte)infoModel.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "type");
	    String units = (String)infoModel.lookupProbeAttributeInfo(m.getProbeID(), aValue.getField(), "units");

	    System.out.print(name + ": " + aValue.getValue() + " " + units + ", ");
	}

	System.out.println();
    }



}