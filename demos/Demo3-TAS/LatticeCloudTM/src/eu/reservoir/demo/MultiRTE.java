// MultiRTE.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.distribution.multicast.MulticastDataPlaneProducer;
import eu.reservoir.monitoring.distribution.multicast.MulticastAddress;
import eu.reservoir.monitoring.appl.datarate.*;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This monitor sends emulated response times  uses a Multicast Data Plane.
 */
public class MultiRTE {
    // The DataSource
    DataSource ds;

    /*
     * Construct a MultiRTE.
     * It has multiple instances of the RandomProbe in order to 
     * generate data from multipe probes and test some thready things.
     */
    public MultiRTE(String addr, int dataPort, String myHostname) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new MulticastDataPlaneProducer(address));

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
	int count = 1;
	String addr = "229.229.0.1";
	int port = 2299;

	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 1) {
	    Scanner sc = new Scanner(args[0]);
	    count = sc.nextInt();
	} else if (args.length == 3) {
	    Scanner sc = new Scanner(args[0]);
	    count = sc.nextInt();
	    
	    addr = args[1];

	    sc = new Scanner(args[2]);
	    port = sc.nextInt();

	} else {
	    System.err.println("MultiRTE [count] [multicast-address port]");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

	// we got a hostname
	MultiRTE hostMon = new MultiRTE(addr, port, currentHost);

	for (int i=0; i<count; i++) {
	    Probe random = new RandomProbe(currentHost + ".elapsedTime", "elapsedTime", 5 + i);

	    random.setServiceID(new ID(12345));
	    random.setGroupID(new ID(2));
	    random.setDataRate(new EveryNMilliseconds(50));

	    hostMon.turnOnProbe(random);
	}
    }


}
