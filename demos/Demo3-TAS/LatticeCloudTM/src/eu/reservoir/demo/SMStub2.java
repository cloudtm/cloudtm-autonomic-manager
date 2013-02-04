// SMStub2.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicConsumer;
import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.distribution.multicast.*;
import eu.reservoir.monitoring.im.dht.*;
import java.util.Scanner;

/**
 * This acts as a stub for the Service Manager.
 * It has to run first, as it is the initial node for
 * the information model.
 * It also listens on the multicast address for measurements.
 */
public class SMStub2 {
    // The Basic consumer
    BasicConsumer consumer;

  /*
   * Construct a the Service Manager stub.
   * It listnes on information plane, 
   * listens on the multicast address for the data plane.
   */
    public SMStub2(String dataAddr, int dataPort, int dhtPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(dataAddr, dataPort);

	// set up data plane
	consumer.setDataPlane(new MulticastDataPlaneConsumer(address));

	// set up info plane
	consumer.setInfoPlane(new DHTInfoPlaneRoot("localhost", dhtPort));

	// set up reporting of messages
	consumer.setReporter(new MeasurementPrinter(consumer.getInfoPlane()));
	

	consumer.connect();

    }


    public static void main(String [] args) {
	if (args.length == 0) {
	    new SMStub2("229.229.0.1", 2299, 6699);
	    System.err.println("Service Manager Stub2 running .....");
	} else if (args.length == 3) {
	    // multicast add
	    String addr = args[0];
	    // multicast port
	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    // dht root port
	    sc = new Scanner(args[2]);
	    int dhtPort = sc.nextInt();

	    new SMStub2(addr, port, dhtPort);
	    System.err.println("Service Manager Stub2 running .....");
	} else {
	    System.err.println("usage: SMStub multicast-address port dhtPort");
	    System.exit(1);
	}
    }
}
