// InfraProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Mar 2010

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.appl.datarate.EveryNSeconds;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * A probe for listing details of some infrastructure.
 */
public class InfraProbe extends AbstractProbe implements Probe  {
    Random randomNo;

    // the vee name == FQN
    String vee;

    // The service name
    String serviceName;

    // The class of measurement for SM
    final static String mClass = "infra_vee";

    // last time
    long prevTime = 0;

    long cpuSecsTotal = 0;

    /*
     * Construct a probe
     */
    public InfraProbe(String vee, String currentHost) {
	this.vee = vee;
        this.serviceName = getServiceName(vee);

        setName(currentHost + "." + vee);
        setDataRate(new EveryNSeconds(15));  // every 15 seconds == 240 samples/hr

	randomNo = new Random();


	// data to be sent is:
	// string FQN name
	// float cpu.percent 
	// long cpu.millis milliseconds of cpu used since last time
	// long cpu.total seconds of cpu used in total
	// long mem.allocated the maximum memory in KBytes allowed
	// long mem.used the memory in KBytes used by the domain
	// int cpu.cores the number of virtual CPUs for the domain
	// char vee.state the running state, one of virDomainFlag
	// long net.rx 
	// long net.tx
	// string serviceName name
	// string class name

        addProbeAttribute(new DefaultProbeAttribute(0, "FQN", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(1, "cpu.percent", ProbeAttributeType.FLOAT, "percent"));
        addProbeAttribute(new DefaultProbeAttribute(2, "cpu.millis", ProbeAttributeType.LONG, "milliseconds"));
        addProbeAttribute(new DefaultProbeAttribute(3, "cpu.total", ProbeAttributeType.LONG, "seconds"));
        addProbeAttribute(new DefaultProbeAttribute(4, "mem.allocated", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(5, "mem.used", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(6, "cpu.cores", ProbeAttributeType.INTEGER, "n"));
        addProbeAttribute(new DefaultProbeAttribute(7, "vee.state", ProbeAttributeType.CHAR, "DomainState"));
        addProbeAttribute(new DefaultProbeAttribute(8, "net.rx", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(9, "net.tx", ProbeAttributeType.LONG, "kilobytes"));
        addProbeAttribute(new DefaultProbeAttribute(10, "serviceName", ProbeAttributeType.STRING, "name"));
        addProbeAttribute(new DefaultProbeAttribute(11, "class", ProbeAttributeType.STRING, "name"));

    }

    /**
     * Begining of thread
     */
    public void beginThreadBody() {
        prevTime = System.currentTimeMillis();
    }
 
    /**
     * Collect a measurement.
     */
    public ProbeMeasurement collect() {
	try {	    

	    // determine the scale factor for normalizing cpu per cent
	    // it depends on the no of milliseconds between each reading
	    float scaleFactor = (float)rationalToMillis(getDataRate());

	    // work out difference for cpu
            int cpuRand = randomNo.nextInt(100);
            long timeNow = System.currentTimeMillis();
	    long cpuMillisD = ((timeNow - prevTime) * cpuRand / 100);
	    float cpuPerCent = cpuRand;
            cpuSecsTotal += (cpuMillisD / 1000);

            // pretend mem usage
            int memRand = randomNo.nextInt(512);
            long memUsage = 1024L + memRand;

	    // network stats 
            long rxRand = randomNo.nextInt(100);
            long txRand = randomNo.nextInt(100);

	    // Convert the state into a byte
	    char state = 'R';

	    //System.out.printf("%5.2f%8d%10d%20s", cpuPerCent, cpuMillisD, cpuSecsTotal, di.memory , di.state);
	    //System.out.printf("%6d%6d\n", ifStats.rx_bytes, ifStats.tx_bytes);

	    ArrayList<ProbeValue> list = new ArrayList<ProbeValue>(12);

	    list.add(new DefaultProbeValue(0, vee));
	    list.add(new DefaultProbeValue(1, cpuPerCent));
	    list.add(new DefaultProbeValue(2, cpuMillisD));
	    list.add(new DefaultProbeValue(3, cpuSecsTotal));
	    list.add(new DefaultProbeValue(4, 2048L));
	    list.add(new DefaultProbeValue(5, memUsage));
	    list.add(new DefaultProbeValue(6, 2));
	    list.add(new DefaultProbeValue(7, state));
	    list.add(new DefaultProbeValue(8, rxRand));
	    list.add(new DefaultProbeValue(9, txRand));
	    list.add(new DefaultProbeValue(10, serviceName));
	    list.add(new DefaultProbeValue(11, mClass));

	    // create the Measurement
	    ProbeMeasurement m = new ProducerMeasurement(this, list, mClass);

            prevTime = timeNow;

	    // return the measurement
	    return m;

	} catch (TypeException te) {
	    System.err.println("TypeException " + te + " in HypervisorProbe");
	    return null;
	}
    }

    /**
     * Determine the serviceName from the FQN.
     */
    protected String getServiceName(String fqn) {
        // currently converts something like
        // es.tid.customers.sun.services.sge1.vees.veemaster.replicas.1
        // into es.tid.customers.sun.services.sge1


        // skip back from end for 2 dots
        int dot1 = fqn.lastIndexOf('.');
        int dot2 = fqn.lastIndexOf('.', dot1-1);
        int dot3 = fqn.lastIndexOf('.', dot2-1);
        int dot4 = fqn.lastIndexOf('.', dot3-1);

        // return from start to dot4
        return fqn.substring(0, dot4);
    }

}
