// DynamicControl.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;
import eu.reservoir.monitoring.core.DataSource;
import eu.reservoir.monitoring.appl.DynamicControl;
import eu.reservoir.monitoring.distribution.multicast.*;

/**
 * This class monitors a user's processes.
 */
public class UserProcControl extends DynamicControl {
    // the start time
    long startTime = 0;

    // the DataSource
    UserProcDataSource dataSource;

    // a list of seen ttys
    LinkedList<String> seenTTYs;
    

    public UserProcControl(String addr, int port, String fqn) {
	super("proc_control");
	// set up data source
	dataSource = new UserProcDataSource(fqn);

	// set up multicast addresses
	MulticastAddress dataGroup = new MulticastAddress(addr, port);

	// set up data plane
	dataSource.setDataPlane(new MulticastDataPlaneProducer(dataGroup));

	dataSource.connect();


    }

    
    /**
     * Initialize.
     */
    protected void controlInitialize() {
	startTime = System.currentTimeMillis();
	seenTTYs = new LinkedList<String>();
    }

    /**
     * Actually evaluate something.
     */
    protected void controlEvaluate() {
	// Run 'who' down the end of a pipe
	// This determines how many ttys we have open
	// Each probe will do detailed analysis
	long now = System.currentTimeMillis();
	long diff = (now - startTime) / 1000;
	System.err.println(diff + ": " + this + " seen " + seenTTYs.size());

	try {
	    Process proc = Runtime.getRuntime().exec("who");
	    InputStream inSteam = proc.getInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inSteam));

	    // read all lines
	    // skip through and determine if there are new ttys
	    String line;
	    LinkedList<String> seenThisTime = new LinkedList<String>();

	    while ((line = reader.readLine()) != null) {
		// each line should look like this:
		//  USER TTY DD MM TIME
		String[] parts = line.split(" ");
		String user = parts[0];
		String tty = parts[1];

		if (seenTTYs.contains(tty)) {
		    // we've already seen this tty
		    seenThisTime.add(tty);
		} else {
		    // we've not seen this tty
		    // so we need to add a probe for it
		    seenThisTime.add(tty);

		    dataSource.addTTYProbe(tty);
		}

	    }

	    // we've got to the end, so now we determine
	    // if there are ttys we have in the list that are
	    // not used any more
	    seenTTYs.removeAll(seenThisTime);

	    if (seenTTYs.size() == 0) {
		// there are no ttys 
	    } else {
		// the are some residual ttys
		// delete the probe for each one
		for (String aTTY : seenTTYs) {
		    dataSource.deleteTTYProbe(aTTY);
		}
	    }

	    // save the current list
	    seenTTYs = seenThisTime;
	    
	    
	} catch (IOException ioe) {
	}
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

	if (args.length == 0) {
	    // use existing settings
	} else if (args.length == 2) {
	    addr = args[0];

	    Scanner sc = new Scanner(args[1]);
	    port = sc.nextInt();
	} else {
	    System.err.println("usage: UserProcControl multicast-address port");
	    System.exit(1);
	}


	// allocate a UserProcControl 
	UserProcControl control = new UserProcControl(addr, port, "userProcControl");

	// activate the  control
	control.activateControl();
    }


}