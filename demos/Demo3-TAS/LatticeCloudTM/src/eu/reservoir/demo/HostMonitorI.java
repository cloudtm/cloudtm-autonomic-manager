// HostMonitorI.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.appl.BasicDataSource;
import eu.reservoir.monitoring.appl.host.linux.*;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.core.Probe;
import eu.reservoir.monitoring.core.ID;
import eu.reservoir.monitoring.distribution.multicast.MulticastDataPlaneProducer;
import eu.reservoir.monitoring.distribution.multicast.MulticastAddress;
import eu.reservoir.monitoring.im.dht.*;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This monitor sends CPU info and uses a Multicast Data Plane.
 * It also uses the Info Plane.
 */
public class HostMonitorI {
    // The DataSource
    DataSource ds;

    /*
     * Construct a HostMonitor.
     */
    public HostMonitorI(String addr, int dataPort, String myHostname, String infoPlaneRootHost, int remotePort, int localPort) {
	// set up data source
	ds = new BasicDataSource(myHostname);

	// set up multicast address for data
	MulticastAddress address = new MulticastAddress(addr, dataPort);

	// set up data plane
	ds.setDataPlane(new MulticastDataPlaneProducer(address));

	ds.setInfoPlane(new DHTInfoPlane(infoPlaneRootHost, remotePort, localPort));
	ds.connect();
    }

    private void turnOnProbe(Probe p) {
	ds.addProbe(p);
	ds.turnOnProbe(p);
    }

    private void turnOffProbe(Probe p) {
	ds.addProbe(p);
	ds.deactivateProbe(p);
    }

    public static void main(String [] args) {
	String flag = "-c";   	// default is CPU

	//  data plane
	String addr = "229.229.0.1";
	int port = 2299;

	// info plane
	String infoRoot = "localhost";
	int infoRootPort = 6699;
	int localPort = 12345;

	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 1) {
	    // set flag for probe
	    flag = args[0];

	} else if (args.length == 3) {
	    // set flag for probe and multicast addr and port
	    flag = args[0];

	    // multicast addr
	    addr = args[1];
	    // multicast port
	    Scanner sc = new Scanner(args[2]);
	    port = sc.nextInt();

	} else if (args.length == 5) {
	    // set flag for probe and multicast addr and port
	    // AND dht root host, root port, 
	    flag = args[0];

	    // multicast addr
	    addr = args[1];
	    // multicast port
	    Scanner sc = new Scanner(args[2]);
	    port = sc.nextInt();

	    // info root host
	    infoRoot = args[3];
	    // info root port
	    sc = new Scanner(args[4]);
	    infoRootPort = sc.nextInt();

	} else if (args.length == 6) {
	    // set flag for probe and multicast addr and port
	    // AND dht root host, root port, 
	    flag = args[0];

	    // multicast addr
	    addr = args[1];
	    // multicast port
	    Scanner sc = new Scanner(args[2]);
	    port = sc.nextInt();

	    // info root host
	    infoRoot = args[3];
	    // info root port
	    sc = new Scanner(args[4]);
	    infoRootPort = sc.nextInt();
	    // info local port
	    sc = new Scanner(args[5]);
	    localPort = sc.nextInt();

	} else {
	    System.err.println("HostMonitor  -c|-n|-m [multicast-address port] [info_plane_host info_plane_port [local_port]]");
	    System.exit(1);
	}

	// try and get the real current hostname
	String currentHost ="localhost";

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}

	// we got a hostname
	HostMonitorI hostMon = new HostMonitorI(addr, port, currentHost, infoRoot, infoRootPort, localPort);

	if (flag.equals("-c")) {
	    Probe cpu = new CPUInfo(currentHost + ".cpuInfo");
	    hostMon.turnOnProbe(cpu);

	} else if (flag.equals("-n")) {
	    Probe net = new NetInfo(currentHost + ".netInfo", "eth0");
	    hostMon.turnOnProbe(net);

	} else if (flag.equals("-m")) {
	    Probe mem = new MemoryInfo(currentHost + ".memoryInfo");
	    hostMon.turnOnProbe(mem);

	} else if (flag.equals("-a")) {
	    Probe cpu = new CPUInfo(currentHost + ".cpuInfo");
	    hostMon.turnOnProbe(cpu);
	    Probe net = new NetInfo(currentHost + ".netInfo", "eth0");
	    hostMon.turnOnProbe(net);
	    Probe mem = new MemoryInfo(currentHost + ".memoryInfo");
	    hostMon.turnOnProbe(mem);


	} else {
	    System.err.println("HostMonitor multicast-address port -c|-n|-m");
	}
    }


}
