// RandomProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.appl.datarate.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * A probe that returns a random float.
 */
public class RandomProbe extends AbstractProbe implements Probe  {
    Random randomNo;
    int scaleFactor;

    /*
     * Construct a probe
     */
    public RandomProbe(String name, String fieldName, int scaleFactor) {
        setName(name);
        setDataRate(new EveryNSeconds(2));
        addProbeAttribute(new DefaultProbeAttribute(0, fieldName, ProbeAttributeType.FLOAT, "milliseconds"));
	randomNo = new Random();
	this.scaleFactor = scaleFactor;
    }


    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {
	    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(1);

	    float next = scaleFactor + (randomNo.nextFloat() * (scaleFactor / 5));;

	    //System.err.println("rand = " + next);

	    list.add(new DefaultProbeValue(0, next));

	    return new ProducerMeasurement(this, list);
	} catch (Exception e) {
	    System.err.println(e);
	    e.printStackTrace();
	    return null;
	}
    }

}
