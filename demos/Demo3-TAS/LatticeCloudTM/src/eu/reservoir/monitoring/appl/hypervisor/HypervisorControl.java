// HypervisorControl.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.appl.hypervisor;

import java.io.*;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Scanner;
import eu.reservoir.monitoring.distribution.multicast.MulticastDataPlaneProducer;
import eu.reservoir.monitoring.distribution.multicast.MulticastAddress;
import eu.reservoir.monitoring.im.dht.*;
import eu.reservoir.monitoring.appl.*;
import org.libvirt.*;

/**
 * This class monitors a user's processes.
 */
public class HypervisorControl extends DynamicControl {
    // The hypervisor cache
    HypervisorCache hypervisor;

    // the Hypervisor URI
    String hypervisorURI;

    // the start time
    long startTime = 0;

    // the HypervisorDataSource
    HypervisorDataSource dataSource;

    // a list of seen vees
    LinkedList<String> seenVEEs;

    // Current hostname
    String currentHost = "localhost";
    

    /**
     * Construct a HypervisorControl, given a specific HypervisorDataSource 
     * and a hypervisor URI such as "xen:///"
     */
    public HypervisorControl(String hypervisorURI) {
	super("hypervisor_control");
	this.hypervisorURI = hypervisorURI;

	// set up the hypervisor connection
	hypervisor = new HypervisorCache(hypervisorURI);
	hypervisor.activateControl();

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}
    }

    public HypervisorControl() {
	super("proc_control");

	// set up the hypervisor connection
	hypervisor = new HypervisorCache(hypervisorURI);
	hypervisor.activateControl();

	try {
	    currentHost = InetAddress.getLocalHost().getHostName();
	} catch (Exception e) {
	}
    }

    
    /**
     * Set the DataSource which creates the probes.
     */
    public HypervisorControl setDataSource(HypervisorDataSource dataSource) {
	this.dataSource = dataSource;
	return this;
    }

    /**
     * Initialize this hypervisor control.
     * It connects to the hypervisor
     */
    protected void controlInitialize() {
	System.err.println("HypervisorControl: controlInitialize()");

	// sleep time is 30 secs
	setSleepTime(10);  // was 10

	startTime = System.currentTimeMillis();
	seenVEEs = new LinkedList<String>();

	// give the DataSource the hypervisor
	dataSource.setHypervisor(hypervisor);

	// wait for the hypervisor cache to be ready
	hypervisor.waitForCacheReady();
    }

    /**
     * Actually evaluate something.
     */
    protected void controlEvaluate() {
	System.err.println("HypervisorControl: controlEvaluate() START");

	// Get the domain list from the hypervisor
	// This determines how many vees we have open
	// Each probe will do detailed analysis
	long now = System.currentTimeMillis();
	long diff = (now - startTime) / 1000;
	System.err.println(diff + ": " + this + " seen " + seenVEEs.size());

	// get the list of current VEEs
	int[] activeDoms = hypervisor.listDomains();

	LinkedList<String> seenThisTime = new LinkedList<String>();

	// get the domain info
	if (hypervisor.numOfDomains() > 0) {
	    for(int d: activeDoms) {
		CachedDomain domain = hypervisor.domainLookupByID(d);
		String vee = domain.getName();

		if (seenVEEs.contains(vee)) {
		    // we've already seen this vee
		    seenThisTime.add(vee);
		} else {
		    // we've not seen this vee
		    // so we need to add a probe for it
		    seenThisTime.add(vee);

		    dataSource.addVEEProbe(d, vee, currentHost);
		}
	    }
	}


	// we've got to the end, so now we determine
	// if there are vees we have in the list that are
	// not used any more
	seenVEEs.removeAll(seenThisTime);

	if (seenVEEs.size() == 0) {
	    // there are no vees 
	} else {
	    // the are some residual vees synchronized
	    // delete the probe for each one
	    for (String aVEE : seenVEEs) {
		dataSource.deleteVEEProbe(aVEE);
	    }
	}

	// save the current list
	seenVEEs = seenThisTime;

	System.err.println("HypervisorControl: controlEvaluate() END");

    }

    /**
     * Cleanup
     */
    protected void controlCleanup() {
   }


    /**
     * Main entry point.
     */
    public static void main(String[] args) {
	String addr = "229.229.0.1";
	int port = 2299;
	String infoRoot = "localhost";
	int infoRootPort = 6699;
	int localPort = 12567;

	
	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 2) {
	    // multicast address
	    addr = args[0];
	    // multicast port
	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	} else if (args.length == 2) {
	    // multicast address
	    addr = args[0];
	    // multicast port
	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	} else if (args.length == 4) {
	    // multicast address
	    addr = args[0];
	    // multicast port
	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	    // info root host
	    infoRoot = args[2];
	    // info root port
	    sc = new Scanner(args[3]);
	    infoRootPort = sc.nextInt();

	} else if (args.length == 5) {
	    // multicast address
	    addr = args[0];
	    // multicast port
	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();

	    // info root host
	    infoRoot = args[2];
	    // info root port
	    sc = new Scanner(args[3]);
	    infoRootPort = sc.nextInt();
	    // info local port
	    sc = new Scanner(args[4]);
	    localPort = sc.nextInt();


	} else {
	    System.err.println("HypervisorControl [multicast-address port] [infoModelHost infoModelPort [local_port]]");
	    System.exit(1);
	}

	// allocate a HypervisorControl to interact with the HypervisorDataSource
	HypervisorControl control = new HypervisorControl("xen:///");

	// allocate a DataSource that can add and delete new PS probes
	HypervisorDataSource dataSource = new HypervisorDataSource("localhost");

	control.setDataSource(dataSource);

	// set up multicast addresses
	MulticastAddress dataGroup = new MulticastAddress(addr, port);
	System.err.println("HypervisorControl running on " + addr + "/" + port + " ....");

	// set up data plane
	dataSource.setDataPlane(new MulticastDataPlaneProducer(dataGroup));

	// set up info plane
	dataSource.setInfoPlane(new DHTInfoPlane(infoRoot, infoRootPort, localPort));

	dataSource.connect();

	// activate the  control
	control.activateControl();
    }


}