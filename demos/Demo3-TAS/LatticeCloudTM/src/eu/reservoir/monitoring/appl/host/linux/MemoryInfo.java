// MemoryInfo.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sep 2009

package eu.reservoir.monitoring.appl.host.linux;

import eu.reservoir.monitoring.core.*;
import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * A probe to get memory info on a Linux system.
 * It uses /proc/meminfo to read the underyling data.
 */
public class MemoryInfo extends AbstractProbe implements Probe  {
    // The /proc/meminfo file
    File procmeminfo = new File("/proc/meminfo");

    // A map of values for the probe
    Map<String, Integer> values = new HashMap<String, Integer>();

    /*
     * Construct a MemoryInfo probe
     */
    public MemoryInfo(String name) {
	setName(name);
        setDataRate(new Rational(360, 1));

        addProbeAttribute(new DefaultProbeAttribute(0, "total", ProbeAttributeType.INTEGER, "kilobytes"));
	addProbeAttribute(new DefaultProbeAttribute(1, "free", ProbeAttributeType.INTEGER, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(2, "used", ProbeAttributeType.INTEGER, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(3, "reallyused", ProbeAttributeType.INTEGER, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(4, "cached", ProbeAttributeType.INTEGER, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(5, "buffers", ProbeAttributeType.INTEGER, "kilobytes"));
 
   }



    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	// create a list the size of the no of attributes
	int attrCount = 6;   // probeAttributes.size();
	ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(attrCount);

	// read the data
	if (read()) {
	    // the relevant data will be in the values map
	    try {
		int memTotal = values.get("MemTotal");
		int memFree = values.get("MemFree");
		int cached = values.get("Cached");
		int buffers = values.get("Buffers");

		int used = memTotal - memFree;
		int reallyUsed = used - (cached + buffers);

		System.err.println("memoryInfo => " +
				       " total = " + memTotal +
				       " free = " + memFree +
				       " used = " + used +
				       " reallyUsed = " + reallyUsed);

		// now collect up the results	
		list.add(new DefaultProbeValue(0, memTotal));
		list.add(new DefaultProbeValue(1, memFree));
		list.add(new DefaultProbeValue(2, used));
		list.add(new DefaultProbeValue(3, reallyUsed));
		list.add(new DefaultProbeValue(4, cached));
		list.add(new DefaultProbeValue(5, buffers));
	    
		return new ProducerMeasurement(this, list, "MemInfo");	
	    } catch (Exception e) {
		return null;
	    }
	} else {
	    System.err.println("Failed to read from /proc/stat");
	    return null;
	}
    }


    /**
     * Read some data from /proc/meminfo
     */
    private boolean read() {
	values.clear();
	if (readProcMeminfo(procmeminfo)) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Read from /proc/meminfo
     *
     * Looking for
     * MemTotal:      6841344 kB
     * MemFree:       3463280 kB
     * Buffers:        109420 kB
     * Cached:        2512396 kB

     */
    private boolean readProcMeminfo(File procmeminfo) {
	String line;
	int count = 0;
	final int needed = 4;  // we are looking for 4 fields

	try {
	    BufferedReader reader = new BufferedReader(new FileReader(procmeminfo));

	    // find all required
	    while ((line = reader.readLine()) != null) {
		if (line.startsWith("MemTotal") || line.startsWith("MemFree") ||
		    line.startsWith("Buffers") || line.startsWith("Cached")) {
		    // it's required info

		    // split gives ["MemFree", "    3463280 kB"]
		    String[] parts = line.split(":");

		    // split gives ["3463280", "kB"]
		    String[] kb = parts[1].trim().split(" ");

		    // put ["MemFree", 3463280]
		    Integer value = Integer.valueOf(kb[0].trim());
		    values.put(parts[0], value);
		    
		    // seen one more
		    count++;
		}

		// check to see if we're finished
		if (count == needed) {
		    // we've reached the end of the fields we need
		    // so we close
		    reader.close();
		    break;
		}
	    }

	    return true;
	} catch (Exception e) {
	    // something went wrong
	    return false;
	}
    }
}

