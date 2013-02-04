// SimpleConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicConsumer;
import eu.reservoir.monitoring.distribution.multicast.MulticastDataPlaneConsumer;
import eu.reservoir.monitoring.distribution.multicast.MulticastAddress;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.Properties;

/**
 * This receives measurements from a Multicast Data Plane.
 */
public class SimpleConsumer {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a SimpleConsumer
     */
    public SimpleConsumer(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new MulticastDataPlaneConsumer(address));

	consumer.connect();

    }

    public static void main(String [] args) {
	Properties props = System.getProperties();
	props.setProperty("java.net.preferIPv4Stack","true");
	System.setProperties(props);

	if (args.length == 0) {
	    new SimpleConsumer("229.229.0.1", 2299);
	    System.err.println("SimpleConsumer listening on 229.229.0.1/2299");
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    new SimpleConsumer(addr, port);

	    System.err.println("SimpleConsumer listening on " + addr + "/" + port);
	} else {
	    System.err.println("usage: SimpleConsumer multicast-address port");
	    System.exit(1);
	}
    }

}