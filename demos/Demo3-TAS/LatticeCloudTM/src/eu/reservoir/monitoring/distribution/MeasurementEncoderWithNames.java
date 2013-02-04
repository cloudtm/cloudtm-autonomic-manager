// MeasurementEncoderWithNames.java
// Author: Fabrizio Pastore
// Date: Feb 2010

package eu.reservoir.monitoring.distribution;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import eu.reservoir.monitoring.core.Measurement;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ProbeAttribute;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.ProbeValue;
import eu.reservoir.monitoring.core.ProducerMeasurement;
import eu.reservoir.monitoring.core.TypeException;

public class MeasurementEncoderWithNames extends MeasurementEncoder {


    /**
     * Encode the Measurement to a DataOutput object.
     * The message is encoded and it's structure is:
     * <pre>
     * +------------------------------------------------------------------------+
     * | seq no (long) | options (byte) [bit 0 : 0 = no names / 1 = with names] |
     * +------------------------------------------------------------------------+
     * |  probe id (long) | type (utf string)  | timestamp (long)               |
     * +------------------------------------------------------------------------+
     * | time delta (long) | service id (long) | group id (long)                |
     * +------------------------------------------------------------------------+
     * | probe name (utf string) | attr count                                   |
     * +------------------------------------------------------------------------+
     * | attr0 field no (int) | attr0 name (utf string) | attr0 type (byte)     |
     * | attr0 value (depends)                                                  |
     * +------------------------------------------------------------------------+
     * | ....                                                                   |
     * +------------------------------------------------------------------------+
     * | attrN field no (int) | attrN name (utf string) | attrN type (byte)     | 
     * | attrN value (depends)                                                  |
     * +------------------------------------------------------------------------+
     * </pre>
     */
    public void encode(DataOutput out) throws IOException, TypeException {
        this.out = out;

        /* write measurement */

        // write seq no
        out.writeLong(measurement.getSequenceNo());
        // write options byte
        // set bit 0 to 1 to indicate that this has names
        out.writeByte((byte)0x01);
        // write probe id
        out.writeLong(measurement.getProbeID().longValue());
        // write measurement type
        out.writeUTF(measurement.getType());
        // write timestamp
        out.writeLong(measurement.getTimestamp().value());
        // write measurement delta
        out.writeLong(measurement.getDeltaTime().value());
        // write out the service ID
        out.writeLong(measurement.getServiceID().longValue());
        // write out the group ID
        out.writeLong(measurement.getGroupID().longValue());
		
        // added by TOF
        // encode probe name
        out.writeUTF(((ProducerMeasurement)measurement).getProbe().getName());

        // write attributes
		
        // count first
        int attrCount = measurement.getValues().size();
        out.writeInt(attrCount);

        //System.err.print(" [" + attrCount + "] ");
		
		
        Probe probe = ((ProducerMeasurement)measurement).getProbe();
		
        // skip through all the attributes
		
		
        for (ProbeValue attr : measurement.getValues()) {			  
            // write attr
            int field = attr.getField();		    
		    
            // first write out the attribute field as an integer
            out.writeInt(field);
		    
            // write the attribute name
            ProbeAttribute attribute = probe.getAttribute(field);
            String name = attribute.getName();
		    
            // then write the type
            Object value = attr.getValue();
            ProbeAttributeType type = ProbeAttributeType.lookup(value);

            //System.err.println("Encoding " + type + " name:" + name + ";" );
		    
            encodeValue(name, ProbeAttributeType.STRING);
		    
            encodeType(type);

            // System.err.print((char)type.getCode());
            // System.err.print(", ");
		    
            // and now the value
            encodeValue(value, type);
		    
		    
        }
		
    }

    /**
     * Construct a MeasurementEncoderWithNames for a Measurement.
     */
    public MeasurementEncoderWithNames(Measurement m) {
        super(m);
    }
	
	

}
