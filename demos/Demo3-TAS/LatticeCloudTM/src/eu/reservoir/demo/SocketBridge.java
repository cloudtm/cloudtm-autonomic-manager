// SocketBridge.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2010

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.distribution.multicast.MulticastDataPlaneProducer;
import eu.reservoir.monitoring.distribution.multicast.MulticastAddress;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This monitor sends data froma socket uses a Multicast Data Plane.
 */
public class SocketBridge {
    // The DataSource
    DataSource ds;

    /*
     * Construct a SocketBridge
     */
    public SocketBridge(String addr, int dataPort, String myHostname) {
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
	ds.activateProbe(p);
    }

    private void turnOffProbe(Probe p) {
	ds.deactivateProbe(p);
	ds.removeProbe(p);
    }

    public static void main(String [] args) {
	String addr = "229.229.0.1";
	int port = 2299;

        int socketPort = 4567;

	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 3) {
	    addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

            sc = new Scanner(args[2]);
	    socketPort = sc.nextInt();

	} else {
	    System.err.println("SocketBridge multicast-address multicast-port socket-port");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

	// we got a hostname
	SocketBridge bridge = new SocketBridge(addr, port, currentHost);

	Probe sockProbe = new SocketProbe(currentHost + ".socket", socketPort);


	bridge.turnOnProbe(sockProbe);

    }


}
