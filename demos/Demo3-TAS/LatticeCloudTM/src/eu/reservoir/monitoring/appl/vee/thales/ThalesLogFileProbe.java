// ThalesLogFileProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Jan 2010

package eu.reservoir.monitoring.appl.vee.thales;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;


/**
 * A probe for gettting the response time.
 */
public class ThalesLogFileProbe extends AbstractProbe implements Probe  {
    /*
     * The file with the data in.
     */
    File logFile = null;

    /*
     * File length
     */
    long length = 0;

    /*
     * Hit eof
     */
    boolean eof = false;
     
    /*
     * A reader for the file.
     */
    BufferedReader reader;

    /**
     * Construct a probe
     */
    public ThalesLogFileProbe(String name, String filename) {
	// set log file
	logFile = new File(filename);
	// get it's length
	length = length();

	// set name
	setName(name);
	// set service ID
setServiceID(new ID(0)); // should set real one

	// once every 1 seconds
	setDataRate(new EveryNSeconds(1));

	// define attributes
	addProbeAttribute(new DefaultProbeAttribute(0, "Value", ProbeAttributeType.INTEGER, "n"));
	addProbeAttribute(new DefaultProbeAttribute(1, "FQN", ProbeAttributeType.STRING, "name"));

	// activate the Probe
	activateProbe();
    }

    /**
     * Turning on the Probe should open the file.
     */
    public ProbeLifecycle turnOnProbe() {
	// open file
	try {
	    reader = new BufferedReader(new FileReader(logFile));	
	    super.turnOnProbe();
	} catch (Exception e) {
	    System.err.println("ThalesLogFileProbe: Cannot open file " + logFile + " so Probe not started.");
	}

	return this;
    }

    /**
     * Turn off the probe closes the file.
     */
    public ProbeLifecycle turnOffProbe() {
	super.turnOffProbe();
	try {
	    reader.close();
	} catch (Exception e) {
	    System.err.println("ThalesLogFileProbe: cannot close " + logFile);
	}

	return this;
    }


    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() { //VG: reimplemented to read the SGE queue length value from a file.
	try {
	    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(3);

	    // read a response time
	    int responseTime = readEntry();

	    if (responseTime == -1) {
		// there is no value in the file
		return null;
	    } else {
		System.err.println(responseTime);
		// add values to list
		list.add(new DefaultProbeValue(0, responseTime));
		list.add(new DefaultProbeValue(1, getName()));

		// create a measurement based on the values
		return new ProducerMeasurement(this, list, getName());
	    }
	} catch (Exception e) {
	    // on error, return a null
	    return null;
	}
    }

    /*
     * We need to take account of the fact that the file grows
     * and that we might reach the end of the file.
     */
    private int readEntry() {
	int rt;

	// see if we hit eof ast time
	if (eof) {
	    // check file length
	    if (length == length()) {
		// still same size
	    } else {
		//System.err.println("File got bigger");
		length = length();
		eof = false;
	    }
	}

	try {
	    // read a line
	    String line = reader.readLine();

	    if (line == null) {
		eof = true;
		return -1;
	    }

	    // split into parts
	    String[] parts = line.split(" ");
	    // part 12 is actually the time
	    rt = Integer.parseInt(parts[11]);

	    return rt;
	} catch (Exception e) {
	    // something went wrong
	    System.err.println("ThalesLogFileProbe: " + e);
	    try {
		reader.close();
	    } catch (Exception ce) {
		System.err.println("ThalesLogFileProbe: cannot close " + logFile);
	    }

	    return 0;
	}
    }

    /**
     * Get file length
     */
    public long length() {
	long len = logFile.length();
	return len;
    }
}