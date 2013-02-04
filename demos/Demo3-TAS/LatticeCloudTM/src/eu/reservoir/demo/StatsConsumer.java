// StatsConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.distribution.*;
import eu.reservoir.monitoring.distribution.multicast.*;
import eu.reservoir.monitoring.appl.DynamicControl;
import eu.reservoir.monitoring.appl.BasicConsumer;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This listens on the multicast address for measurements.
 */
public class StatsConsumer extends DynamicControl implements Reporter {
    /**
     * This receives measurements .
     */
    BasicConsumer consumer;

    /**
     * Total counted so far
     */
    int total = 0;

    int totalMessages = 0;

    /**
     * Total in last time slice
     */
    int slice = 0;

    int sliceMessages = 0;

    /**
     * Last seq no
     */
    HashMap <ID, Integer>lastSeqNo = new HashMap<ID, Integer>();

    /**
     * Lost messages
     */
    int lost = 0;

    /*
     * This listens in the multicast address for measurements.
     */
    public StatsConsumer(String addr, int port) {
	super("stats");

	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(addr, port);

	// set up data plane
	consumer.setDataPlane(new MulticastDataPlaneConsumer(address));

	// set up  reporting of messages
	// this object acts as its own reporter
	consumer.setReporter(this);

	// connect to the Data Plane
	boolean d = consumer.connect();

	// activate
	activateControl();
    }

    /**
     * Reporter of a measurment.
     */
    public void report(Measurement m) {

	//System.out.print(".");

	// another message
	totalMessages++;
	sliceMessages++;

	// we expect a measurement that is actually a ConsumerMeasurement
	// with metadata bound into it
	if (m instanceof ConsumerMeasurementWithMetaData) {
	    // get the transmissionMetaData out of the measurement
	    MetaData metaData = ((ConsumerMeasurementWithMetaData)m).getTransmissionMetaData();

	    // we expect the MetaData to be MulticastTransmissionMetaData
	    if (metaData instanceof MulticastTransmissionMetaData) {
		// get how many bytes were transmitted this time
		int count = ((MulticastTransmissionMetaData)metaData).length;
		// increase the total over all time
		total += count;
		// increase the count for this slice of time
		slice += count;
	    }

	    // now get the messageMetaData out of the measurement
	    metaData = ((ConsumerMeasurementWithMetaData)m).getMessageMetaData();
	    // we expect the MetaData to be MessageMetaData
	    if (metaData instanceof MessageMetaData) {
		int seqNo = ((MessageMetaData)metaData).seqNo;
		ID dataSourceID = ((MessageMetaData)metaData).dataSourceID;

		// check lastSeqNo for this probe
		if (lastSeqNo.get(dataSourceID) == null) {
		    // this is the first measurement for this probe
		} else {
		    // check if lost one
		    if (seqNo == 0) {
			// the data source got reset
			// so don;t worry
		    } else {
			// next measurement
			int expected = lastSeqNo.get(dataSourceID) + 1;
			if (seqNo != expected) {
			    lost += (seqNo - expected);
			}
		    }
		}

		// save seqNo
		lastSeqNo.put(dataSourceID, seqNo);
				    
	    }
	    
	}
    }

    /**
     * Initialize.
     */
    protected void controlInitialize() {
	total = 0;
	slice = 0;
	System.out.println("elapsed timeslice timesliceN total totalN lost");
    }

    /**
     * Actually evaluate something.
     */
    protected void controlEvaluate() {
	int elapsed = getElapsedTime()/1000;
	System.out.printf("%-7s%10d%10d%7d%7d%5d\n" , (elapsed+":"), slice, sliceMessages, total, totalMessages, lost);

	slice = 0;
	sliceMessages = 0;
    }

    /**
     * Cleanup
     */
    protected void controlCleanup() {
    }


    public static void main(String [] args) {
	if (args.length == 0) {
	    StatsConsumer consumer = new StatsConsumer("229.229.0.1", 2299);
	    consumer.setSleepTime(60);
	    System.err.println("StatsConsumer running .....");
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    StatsConsumer consumer = new StatsConsumer(addr, port);
	    consumer.setSleepTime(60);
	    System.err.println("StatsConsumer running .....");
	} else if (args.length == 3) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    sc = new Scanner(args[2]);
	    int sleep = sc.nextInt();

	    StatsConsumer consumer = new StatsConsumer(addr, port);
	    consumer.setSleepTime(sleep);
	    System.err.println("StatsConsumer running .....");
	} else {
	    System.err.println("usage: StatsConsumer multicast-address port [sleep-secs]");
	    System.exit(1);
	}
    }
}
