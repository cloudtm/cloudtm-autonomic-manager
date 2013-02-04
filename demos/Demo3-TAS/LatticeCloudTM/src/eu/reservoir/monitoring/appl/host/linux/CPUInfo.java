// CPUInfo.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.appl.host.linux;

import eu.reservoir.monitoring.core.*;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * A probe to get cpu info on a Linux system.
 * It uses /proc/stat to read the underyling data.
 */
public class CPUInfo extends AbstractProbe implements Probe  {
    // The /proc/stat file
    File procstat = new File("/proc/stat");

    // a map of the last snapshot from /proc/stat
    HashMap<String, Object> lasttime = new HashMap<String, Object>();
    // a map of the current delta values from /proc/stat
    HashMap<String, Object> thisdelta = new HashMap<String, Object>();
    //a map of the total usage for each cpu
    HashMap<String, Object> lasttotal = new HashMap<String, Object>();

    // no of cpus
    int cpuCount = 0;


    /*
     * Construct a CPUInfo probe
     */
    public CPUInfo(String name) {
	setName(name);
        setDataRate(new Rational(360, 1));

	// read data, but calculate nothing
	read(false);

	// determine actual attributes
	// skip through all keys of last read() to determine the attributes
	int field = 0;
	// sort the keys (not essentail, for easy reading)
	ArrayList<String> keyList = new ArrayList<String>(lasttime.keySet());
	Collections.sort(keyList);

	for (String key : keyList) {
	    // add extra 2 for these fields
	    if (field % (cpuCount+2) == 0) {
		// add cpu name
		addProbeAttribute(new DefaultProbeAttribute(field, "name", ProbeAttributeType.STRING, "name"));
		field++;

		// add total
		addProbeAttribute(new DefaultProbeAttribute(field, "total", ProbeAttributeType.INTEGER, "n"));
		field++;
	    }
	    
	    addProbeAttribute(new DefaultProbeAttribute(field, key, ProbeAttributeType.FLOAT, "percent"));
	    field++;
	}
    }

    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	// create a list the size of the thisdelta map
	ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(thisdelta.size());

	// read the data
	if (read(true)) {
	    try {
		// now collect up the results	
		int field = 0;
		// sort the keys (not essentail, for easy reading)
		ArrayList<String> keyList = new ArrayList<String>(thisdelta.keySet());
		Collections.sort(keyList);

		for (String key : keyList) {
		    // add cpu name and total
		    if (field % (cpuCount+2) == 0) {
			// current cpu name
			String[] parts = key.split("-");
			String cpuName = parts[0];
			list.add(new DefaultProbeValue(field, cpuName));
			field++;
			list.add(new DefaultProbeValue(field, lasttotal.get(cpuName)));
			field++;
		    }

		    list.add(new DefaultProbeValue(field, thisdelta.get(key)));
		    field++;
		}
	    
		return new ProducerMeasurement(this, list, "CPUInfo");	
	    } catch (Exception e) {
		return null;
	    }
	} else {
	    System.err.println("Failed to read from /proc/stat");
	    return null;
	}
    }

    /**
     * Read some data from /proc/stat.
     * If calculate is true, then calculate the deltas between 
     * this read and the last read.
     */
    private boolean read(boolean calculate) {
	int millis = (int)(getDataRate().reciprocal().toDouble() * 3600 * 1000);

        // timeout is in milliseconds, jiffies is in 100ths
        int jiffies =  millis / 1000;

	List<String> results = readProcStat(procstat);

	if (results == null) {
	    return false;

	} else {

	    for (String infoLine : results) {
		String[] parts = infoLine.split(" ");

		String cpu = parts[0];
		int userN = Integer.parseInt(parts[1]);
		int niceN = Integer.parseInt(parts[2]);
		int systemN = Integer.parseInt(parts[3]);
		int idleN = Integer.parseInt(parts[4]);

		//System.err.println("data => " + cpu +
		// " user = " + userN +
		// " nice = " + niceN +
		// " system = " + systemN +
		// " idle = " + idleN);

		int total = userN + niceN + systemN + idleN;

		// determine if we need to calculate the deltas
		// from the raw data
		if (calculate) {  // as a %age
		    int userDiff = userN - (Integer)lasttime.get(cpu+"-user");
		    int niceDiff = niceN - (Integer)lasttime.get(cpu+"-nice");
		    int systemDiff = systemN - (Integer)lasttime.get(cpu+"-system");
		    int idleDiff = idleN - (Integer)lasttime.get(cpu+"-idle");

		    System.err.println("cpuInfo => " + cpu + ":" +
				       " user = " + userDiff +
				       " nice = " + niceDiff +
				       " system = " + systemDiff +
				       " idle = " + idleDiff);

		    thisdelta.put(cpu+"-user", (float) (userDiff / jiffies));
		    thisdelta.put(cpu+"-nice", (float) (niceDiff / jiffies));
		    thisdelta.put(cpu+"-system", (float) (systemDiff / jiffies));
		    thisdelta.put(cpu+"-idle", (float) (idleDiff / jiffies));
		}

		lasttime.put(cpu+"-user", userN);
		lasttime.put(cpu+"-nice", niceN);
		lasttime.put(cpu+"-system", systemN);
		lasttime.put(cpu+"-idle", idleN);
		lasttotal.put(cpu, total);
	    }
	}

	return true;
    }

    /**
     * Read from /proc/stat
     */
    private List<String> readProcStat(File procstat) {
	LinkedList<String> cpuInfo = new LinkedList<String>();
	String line;

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(procstat));

	    // find all lines starting with cpu
	    while ((line = reader.readLine()) != null) {
		if (line.startsWith("cpu")) {
		    // it's cpu info
		    cpuInfo.add(line);
		} else {
		    // we've reached the end of the cpu stat lines
		    // so we close
		    reader.close();
		    break;
		}
	    }
	} catch (Exception e) {
	    // something went wrong
	    return null;
	}

	// now we do a bit of post processing
	if (cpuInfo.size() == 1) {
	    // there is only one cpu, so return the info
	    cpuCount = 1;
	    return cpuInfo;
	} else {
	    // there is more than one.
	    // the first entry is a summation, so we drop it
	    cpuCount = cpuInfo.size() - 1;
	    cpuInfo.remove(0);
	    return cpuInfo;
	}
    }
}