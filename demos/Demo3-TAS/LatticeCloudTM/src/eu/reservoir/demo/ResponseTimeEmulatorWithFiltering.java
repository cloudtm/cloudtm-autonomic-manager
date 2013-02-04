// ResponseTimeEmulator.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.distribution.multicast.MulticastDataPlaneProducer;
import eu.reservoir.monitoring.distribution.multicast.MulticastAddress;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This monitor sends emulated response times  uses a Multicast Data Plane.
 */
public class ResponseTimeEmulatorWithFiltering {
    // The DataSource
    DataSource ds;

    /*
     * Construct a ResponseTimeEmulator.
     */
    public ResponseTimeEmulatorWithFiltering(String addr, int dataPort, String myHostname) {
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
	ResponseTimeEmulatorWithFiltering hostMon = new ResponseTimeEmulatorWithFiltering(addr, port, currentHost);


	// create probe
	Probe random = new RandomProbe(currentHost + ".elapsedTime", "elapsedTime", 15);

	// set service ID and group ID
	random.setServiceID(new ID(12345));
	random.setGroupID(new ID(2));


	// define a filter for the probe
	// this has a +/- 5% tolerance from the previous value
	ProbeFilter filter = new ProbeFilter() {
		public String getName() { return "probe-filter"; }

		public boolean filter(Probe p, Measurement m) {
		    Measurement oldValue = p.getLastMeasurement();
		    
		    if (oldValue == null) {
			// no old value, so always return true
			return true;
		    } else {
			Number old = (Number)oldValue.getValues().get(0).getValue();
			Number newValue = (Number)m.getValues().get(0).getValue();
			double n = newValue.doubleValue();

			double lowerBound = 0.95 * old.doubleValue();
			double upperBound = 1.05 * old.doubleValue();

			System.out.println("Filter: n = " + n + ", old = " + old + " L = " +
					   lowerBound + " U = " + upperBound);

			// test for 5% tolerance -  0.95 -> 1.05
			if (lowerBound < n && n  < upperBound) {
			    // values too similar
			    return false;
			} else {
			    return true;
			}
		    }
		}
	    };

	// set the filter
	random.setProbeFilter(filter);
	// and turn on filtering
	random.turnOnFiltering();

	// turn on probe
	hostMon.turnOnProbe(random);

    }

    /**
     * Define a filter for the Probe.
     */


}
