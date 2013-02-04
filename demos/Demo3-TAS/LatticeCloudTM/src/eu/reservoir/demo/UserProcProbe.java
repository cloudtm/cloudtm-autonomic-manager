// UserProcProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * A probe for listing processes on a tty
 */
public class UserProcProbe extends AbstractProbe implements Probe  {
    // the tty name
    String tty;

    /*
     * Construct a probe
     */
    public UserProcProbe(String tty) {
	this.tty = tty;
        setName(tty);
        setDataRate(new Rational(360, 1));

	//   PID TTY           TIME CMD
	// 52306 ttys000    0:03.27 login -pf sclayman
	// 52316 ttys000    0:00.14 -bash
	// 96754 ttys000    0:00.01 ps -t ttys000

        addProbeAttribute(new DefaultProbeAttribute(0, "pid", ProbeAttributeType.INTEGER, "PID"));
        addProbeAttribute(new DefaultProbeAttribute(1, "tty", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(2, "time", ProbeAttributeType.STRING, "time"));
        addProbeAttribute(new DefaultProbeAttribute(3, "cmd", ProbeAttributeType.STRING, "name"));

        activateProbe();
    }


    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {
	    Process proc = Runtime.getRuntime().exec("ps -rt " + tty);
	    InputStream inSteam = proc.getInputStream();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(inSteam));

	    // read 2 lines
	    String line;
	    
	    // line 1 is header
	    if ((line = reader.readLine()) == null) {
		// EOF on line 1
		System.err.println(getName() + ": EOF on header");
		// get the probe manager to turn off this probe
		// and deactivate it
		getProbeManager().turnOffProbe(this);
		getProbeManager().deactivateProbe(this);
		return null;
	    } else {
		if ((line = reader.readLine()) == null) {
		    // EOF on line 2
		    System.err.println(getName() + ": EOF on line 1");
		    // get the probe manager to turn off this probe
		    // and deactivate it
		    getProbeManager().turnOffProbe(this);
		    getProbeManager().deactivateProbe(this);
		    return null;
		} else {
		    // each line should look like this:
		    //  PID TTY TIME CMD
		    Scanner scanner = new Scanner(line);
		    int pid = scanner.nextInt();
		    String tty = scanner.next();
		    String time = scanner.next();
		    String cmd = scanner.findInLine(".*");


		    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(4);

		    list.add(new DefaultProbeValue(0, pid));
		    list.add(new DefaultProbeValue(1, tty));
		    list.add(new DefaultProbeValue(2, time));
		    list.add(new DefaultProbeValue(3, cmd));

		    ProbeMeasurement m = new ProducerMeasurement(this, list, "TTY");

		    System.out.println(m.getValues());

		    // close process
		    proc.getInputStream().close();
		    proc.getOutputStream().close();
		    proc.waitFor();
		    return m;
		}
	    }
	} catch (IOException ioe) {
	    System.err.println("IOException in UserProcProbe");
	    return null;
	} catch (InterruptedException ie) {
	    System.err.println("InterruptedException in UserProcProbe");
	    return null;
	} catch (Exception e) {
	    System.err.println("Exception " + e + " in UserProcProbe");
	    return null;
	}
    }

}
