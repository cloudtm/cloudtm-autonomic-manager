// ResponseTimeEmulator.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.distribution.multicast.MulticastDataPlaneProducerNoNames;
import eu.reservoir.monitoring.distribution.multicast.MulticastAddress;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This monitor sends emulated response times  uses a Multicast Data Plane.
 */
public class ResponseTimeEmulator {
    // The DataSource
    DataSource ds;

    /*
     * Construct a ResponseTimeEmulator.
     */
    public ResponseTimeEmulator(String addr, int dataPort, String myHostname) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new MulticastDataPlaneProducerNoNames(address));

	ds.connect();
    }

    private void turnOnProbe(Probe p) {
	ds.addProbe(p);
	ds.turnOnProbe(p);
    }

    private void turnOffProbe(Probe p) {
	ds.deactivateProbe(p);
	ds.removeProbe(p);
    }

    public static void main(String [] args) {
	String addr = "229.229.0.1";
	int port = 2299;

	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 2) {
	    addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	} else {
	    System.err.println("ResponseTimeEmulator multicast-address port");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

	// we got a hostname
	ResponseTimeEmulator hostMon = new ResponseTimeEmulator(addr, port, currentHost);

	Probe random = new RandomProbe(currentHost + ".elapsedTime", "elapsedTime", 15);

	random.setServiceID(new ID(12345));
	random.setGroupID(new ID(2));

	hostMon.turnOnProbe(random);

    }


}
