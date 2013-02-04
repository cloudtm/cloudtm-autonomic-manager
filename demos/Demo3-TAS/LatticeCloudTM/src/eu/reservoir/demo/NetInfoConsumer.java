// NetInfoConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Nov 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicConsumer;
import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.distribution.multicast.*;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.Properties;
import java.util.List;

/**
 * This receives measurements from a Multicast Data Plane.
 */
public class NetInfoConsumer {
    // The Basic consumer
    BasicConsumer consumer;

    /*
     * Construct a NetInfoConsumer
     */
    public NetInfoConsumer(String addr, int dataPort) {
	// set up a BasicConsumer
	consumer = new BasicConsumer();

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(addr, dataPort);

	// set up data plane
	consumer.setDataPlane(new MulticastDataPlaneConsumer(address));

	// set up printer of messages
	consumer.setReporter(new NetInfoM());

	consumer.connect();

    }

    public static void main(String [] args) {
	Properties props = System.getProperties();
	props.setProperty("java.net.preferIPv4Stack","true");
	System.setProperties(props);

	if (args.length == 0) {
	    new NetInfoConsumer("229.229.0.1", 2299);
	    System.err.println("NetInfoConsumer listening on 229.229.0.1/2299");
	} else if (args.length == 2) {
	    String addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    int port = sc.nextInt();

	    new NetInfoConsumer(addr, port);

	    System.err.println("NetInfoConsumer listening on " + addr + "/" + port);
	} else {
	    System.err.println("usage: NetInfoConsumer multicast-address port");
	    System.exit(1);
	}
    }

}

class NetInfoM implements Reporter {
    /**
     * Receiver of a measurment.
     */
    public void report(Measurement m) {
	List<ProbeValue> values = m.getValues();

	System.out.printf("%10d%10d%10d%10d\n" , 
			  values.get(0).getValue(),
			  values.get(1).getValue(), 
			  values.get(4).getValue(), 
			  values.get(5).getValue());

    }
}