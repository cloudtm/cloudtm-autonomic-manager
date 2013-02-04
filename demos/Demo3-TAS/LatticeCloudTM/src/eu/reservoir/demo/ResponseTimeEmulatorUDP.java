// ResponseTimeEmulatorUDP.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.distribution.udp.UDPDataPlaneProducer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * This monitor sends emulated response times  uses a UDP Data Plane.
 */
public class ResponseTimeEmulatorUDP {
    // The DataSource
    DataSource ds;

    /*
     * Construct a ResponseTimeEmulator.
     */
    public ResponseTimeEmulatorUDP(String addr, int dataPort, String myHostname) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up an IPaddress for data
	//InetAddress dataAddr = InetAddress.getByName(addr);
	InetSocketAddress address = new InetSocketAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new UDPDataPlaneProducer(address));

	ds.connect();
    }

    private void turnOnProbe(Probe p) {
	if (ds.isConnected()) {
	    ds.addProbe(p);
	    ds.turnOnProbe(p);
	}
    }

    private void turnOffProbe(Probe p) {
	if (ds.isConnected()) {
	    ds.deactivateProbe(p);
	    ds.removeProbe(p);
	}
    }

    public static void main(String [] args) {
	String addr = "localhost";
	int port = 22997;

	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 2) {
	    addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	} else {
	    System.err.println("ResponseTimeEmulatorUDP ip-address port");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

	// we got a hostname
	ResponseTimeEmulatorUDP hostMon = new ResponseTimeEmulatorUDP(addr, port, currentHost);

	String unique = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
	Probe random = new RandomProbe(currentHost + ".elapsedTime" + "." + unique, "elapsedTime", 15);
	hostMon.turnOnProbe(random);

    }


}
