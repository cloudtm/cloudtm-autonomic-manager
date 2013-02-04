// DataPlaneMessage.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core.plane;

import eu.reservoir.monitoring.core.DataSource;
import java.net.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This implements the header parts for the Data Plane messages.
 */
public abstract class DataPlaneMessage {
    /*
     * Instance variables
     */

    // the DataSource this is a message from
    DataSource dataSource;

    // the protocol type, e.g. ANNOUNCE
    MessageType type;

    // the sequence number, e.g. 154
    int seqNo;

    // the time of day since epoch, 
    long timeOfDay;

    /**
     * Construct a DataPlaneMessage object.
     */
    public DataPlaneMessage() {
	timeOfDay = System.currentTimeMillis();
    }

    /**
     * Get the type.
     */
    public MessageType getType() {
	return type;
    }
    
    /**
     * Get the time of day the Protocol was created
     */
    public long getTimeOfDay() {
	return timeOfDay;
    }
    

    /**
     * Get the sequence no.
     */
    public int getSeqNo() {
	return seqNo;
    }

    /**
     * Set the sequence number of the next message.
     */
    public void setSeqNo(int seq) {
	seqNo = seq;
    }

    /**
     * Get the DataSource
     */
    public DataSource getDataSource() {
	return dataSource;
    }

    /**
     * Set the DataSource for the next message.
     */
    public void setDataSource(DataSource ds) {
	dataSource = ds;
    }

    /**
     * A toString() rewrite
     */
    public String toString() {
	return type + " => " + seqNo + " @ " + timeOfDay;
    }
}
