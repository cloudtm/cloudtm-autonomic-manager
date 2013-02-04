// JavaMemoryProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.*;
import java.util.ArrayList;

/**
 * A probe for finding out how much memory the Java runtime is using
 */
public class JavaMemoryProbe extends AbstractProbe implements Probe  {
    // The Java runtime
    static Runtime runtime = Runtime.getRuntime();

    /*
     * Construct a probe
     */
    public JavaMemoryProbe(String name) {
        setName(name);
        setDataRate(new Rational(360, 1));
        addProbeAttribute(new DefaultProbeAttribute(0, "free", ProbeAttributeType.INTEGER, "megabytes"));
        addProbeAttribute(new DefaultProbeAttribute(1, "used", ProbeAttributeType.INTEGER, "megabytes"));
        addProbeAttribute(new DefaultProbeAttribute(2, "total", ProbeAttributeType.INTEGER, "megabytes"));
        addProbeAttribute(new DefaultProbeAttribute(3, "max", ProbeAttributeType.INTEGER, "megabytes"));
        activateProbe();
    }


    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {
	    int onemeg = (int)(1024 * 1024);
	    int free = (int)(runtime.freeMemory() / onemeg);
	    int total = (int)(runtime.totalMemory() / onemeg);
	    int used = (int)(total - free / onemeg);
	    int max = (int)(runtime.maxMemory() / onemeg);

	    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(4);

	    list.add(new DefaultProbeValue(0, free));
	    list.add(new DefaultProbeValue(1, used));
	    list.add(new DefaultProbeValue(2, total));
	    list.add(new DefaultProbeValue(3, max));

	    return new ProducerMeasurement(this, list);
	} catch (Exception e) {
	    return null;
	}
    }

}
