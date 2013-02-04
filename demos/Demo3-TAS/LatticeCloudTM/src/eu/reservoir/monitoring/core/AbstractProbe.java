// AbstractProbe.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

// Added code that guarantees that N collect() are called if N inform() 
// are invoked.  Fabrizio Pastore  Jan 2010.

package eu.reservoir.monitoring.core;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An abstract probe.
 */
public abstract class AbstractProbe implements Probe {
    /*
     * The ID
     */
    ID myID;

    /*
     * The probe name
     */
    String name = "probe";

    /*
     * probe status - on or off
     */
    Probe.Status probeStatus = Probe.Status.OFF;

    /*
     * The data rate as a Rational.
     * Specified in measurements per hour
     */
    Rational dataRate = null;

    /*
     * The Service ID for this probe
     */
    ID serviceID;

    /*
     * The Group ID for this probe
     */
    ID groupID;

    /*
     * The last time a measurement was collected.
     */
    Timestamp lastMeasurementTime = null;

    /*
     * The last measurement
     */
    Measurement lastMeasurement = null;

    /*
     * The sequence no for measurements.
     */
    long probeSeqNo = 0;

    /*
     * The thread that this probe runs in
     */
    Thread myThread = null;

    /*
     * Thread running?
     */
    boolean threadRunning = false;

    /*
     * The ProbeManager for this Probe.
     */
    ProbeManager manager = null;

    /*
     * The data dictionary
     */
    Map<Integer, ProbeAttribute> probeAttributes = new LinkedHashMap<Integer, ProbeAttribute>();

    /*
     * The Collection type.
     */
    Probe.CollectionType collectiontype = Probe.CollectionType.OnEvent;

    /*
     * The Reporting type.
     */
    Probe.ReportingType reportingtype = Probe.ReportingType.AtDataRate;

    /*
     * The ProbeFilter for this probe.
     */
    ProbeFilter filter;

    /*
     * Is the Probe Filtering
     */
    boolean isFiltering = false;

    /*
     * The number of informs() events the Probe has had.
     */
    AtomicInteger informsToProcess = new AtomicInteger();
	


    /**
     * Construct a probe
     */
    public AbstractProbe() {
	// convert the name into a hashcode and then into an unsigned long
	myID = new ID(0xffffffffL & getName().hashCode());
	serviceID = new ID(0);
	groupID = new ID(0);
    }

    /*
     * Get the name of the Probe
     */
    public String getName() {
	return name;
    }

    /*
     * Set the name of the Probe
     */
    public Probe setName(String aName) {
	name = aName;
	// convert the name into a hashcode and then into an unsigned long
	myID = new ID(0xffffffffL & getName().hashCode());
	return this;
    }

    /**
     * Get the Probe ID
     */
    public ID getID() {
	return myID;
    }

    /**
     * Set the Probe ID
     */
    public Probe setID(ID id) {
	myID = id;
	return this;
    }

    /**
     * Is this Probe turned on.
     * The thread is running, but is the Probe getting values.
     */
    public boolean isOn() {
	return probeStatus.equals(Probe.Status.ON);
    }

    /**
     * Has this probe been activated.
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isActive() {
	return threadRunning;
    }

    /**
     * Get the Service ID of the Probe.
     */
    public ID getServiceID() {
	return serviceID;
    }

    /**
     * Set the Service ID for a Probe
     */
    public Probe setServiceID(ID id) {
	serviceID = id;
	return this;
    }


    /**
     * Get the Group ID of the Probe.
     */
    public ID getGroupID() {
	return groupID;
    }

    /**
     * Set the Group ID for a Probe
     */
    public Probe setGroupID(ID id) {
	groupID = id;
	return this;
    }

    /**
     * Get the probe meta data.
     * Returns a  map of probe attributes,
     * e.g  [name: "memory", dataRate: 720/1, active: true, on: false]
     */
    public Map<String, Object> getMetaData() {
	// return this map[name: name, dataRate: dataRate, active: threadRunning, on: probeStatus == Probe.Status.ON]
	Map<String, Object> md = new LinkedHashMap<String, Object> ();
	md.put("name", name);
	md.put("dataRate", dataRate);
	md.put("active", threadRunning);
	md.put("on", isOn());

	return md;
    }

    /**
     * Get the manager of a Probe.
     */
    public ProbeManager getProbeManager() {
	return manager;
    }

    /**
     * Set the manager of a Probe.
     */
    public ProbeLifecycle setProbeManager(ProbeManager pm) {
	manager = pm;
	return this;
    }

    /**
     * Get the data rate
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getDataRate() {
	return dataRate;
    }

    /**
     * Get the last measurement that was collected.
     */
    public Measurement getLastMeasurement() {
	return lastMeasurement;
    }

    /**
     * Get the last time a measurement was collected.
     */
    public Timestamp getLastMeasurementCollection() {
	return lastMeasurementTime;
    }

    /**
     * Define an element of the data dictionary
     */
    public Probe addProbeAttribute(ProbeAttribute attribute) {
	probeAttributes.put(attribute.getField(), attribute);
	return this;
    }


    /**
     * Get the Probe's Attributes.
     */
    public Collection<ProbeAttribute> getAttributes() {
	return probeAttributes.values();
    }
      
    /**
     * Get the ProbeAttribute with field no. N.
     */
    public ProbeAttribute getAttribute(int n) {
	return probeAttributes.get(n);
    }

    /**
     * Check that a measurement's ProbeValues respond to the 
     * ProbeAttributes defined for a Probe.
     */
    boolean checkMeasurement(Measurement meas) {
	ProducerMeasurement m;

        if (meas == null) {
            return false;
        } else if (meas instanceof ProducerMeasurement) {
	    m = (ProducerMeasurement)meas;
	} else {
	    throw new MeasurementException("In Probe: " + meas.getProbeID() +
					   " Measurements not of class ProducerMeasurement");
	}

        // get attributes and values

	Collection<ProbeAttribute> attributes = getAttributes();
	List<ProbeValue> values = m.getValues();

	// Check if count is same
	if (values.size() != probeAttributes.size()) {
	    // If they are different then the
	    // attributes and values do not tie up
	    throw new MeasurementException("In Probe: " + m.getProbeID() +
					   ". Error " + values.size() + 
					   " ProbeValues submitted. Expected " +
					   attributes.size());
	} else {
	    // now check individual attributes and values
	    // skip through every ProbeAttribute
	    for (ProbeAttribute pa : attributes) {
		// check each ProbeValue against the ProbeAttribute

		// get the field no
		int field = pa.getField();

		// get associated ProbeValue
		ProbeValue pv = values.get(field);

		// check if field exists
		if (pv == null) {
		    // that field was not defined
		    throw new MeasurementException("In Probe: " + m.getProbeID() +
						   " ProbeValue " + field + " of " +
						   attributes.size() + " is missing");
		} else {
		    // check a value against an attribute
		    if (checkValueWithAttribute(pv, pa)) {
			// this one is OK, skip to ext one
		    } else {
			throw new MeasurementException("In Probe: " + m.getProbeID() +
						       " ProbeValue " + field + " has type " +
						       pv.getType() + ". Expected type " +
						       pa.getType());
		    }
		}
	    }

	    // all OK
	    return true;

	}
    }

    /**
     * Check a ProbeValue against a ProbeAttribute.
     * The field no and the type must be the same.
     */
    boolean checkValueWithAttribute(ProbeValue pv, ProbeAttribute pa) {
	// when we get here we know the field numbers are the same
	//  check types
	if (pv.getType().equals(pa.getType())) {
	    // types are same
	    return true;
	} else {
	    return false;
	}
    }


    /**
     * Get the CollectionType of this Probe.
     * Either one of AtDataRate or OnEvent
     */
    public Probe.CollectionType getCollectionType() {
	return collectiontype;
    }

    /**
     * Set the data rate
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Probe setDataRate(Rational rate) {
	collectiontype = Probe.CollectionType.AtDataRate;
	dataRate = rate;
	return this;
    }

    /**
     * Get the current filter.
     */
    public ProbeFilter getProbeFilter() {
	return filter;
    }

    /**
     * Set the current filter.
     * Returns the previous filter.
     */
    public ProbeFilter setProbeFilter(ProbeFilter f) {
	ProbeFilter oldFilter = filter;
	filter = f;

	if (filter == null) {
	    // filter is set to null so turn off filtering
	    turnOffFiltering();
	}

	return oldFilter;
    }

    /**
     * Turn on filtering.
     */
    public ProbeReporting turnOnFiltering() {
	if (reportingtype == Probe.ReportingType.AtDataRate) {
	    if (filter == null) {
		// can't do filtering with no filter
	    } else {
		// set filtering
		reportingtype = Probe.ReportingType.OnChange;
		isFiltering = true;
	    }
	}

	return this;
    }

    /**
     * Turn off filtering.
     */
    public ProbeReporting turnOffFiltering() {
	if (reportingtype == Probe.ReportingType.OnChange) {
	    reportingtype = Probe.ReportingType.AtDataRate;
	    isFiltering = false;
	}

	return this;
    }

    /**
     * Is the probe filtering values.
     */
    public boolean isFiltering() {
	return isFiltering;
    }

    /**
     * Turn on the probe
     */
    public ProbeLifecycle turnOnProbe() {
	setStatus(Probe.Status.ON);
	return this;
    }

    /**
     * Turn off the probe
     */
    public ProbeLifecycle turnOffProbe() {
	setStatus(Probe.Status.OFF);
	return this;
    }

    /**
     * Set the status of the probe
     */
    private void setStatus(Probe.Status status) {
	if (status == Probe.Status.OFF) {        // try and turn the probe off
	    // now check the current status
	    if (probeStatus == Probe.Status.OFF) {
		// it's already off, so nothing to do
	    } else {
		// the Probe Status is on
		// so turn off this probe
		probeStatus = status;

		if (collectiontype ==  Probe.CollectionType.AtDataRate) {
		    kickThreadOff();
		}
	    }
	} else {      // try and turn the probe on
	    // now check the current status
	    if (probeStatus == Probe.Status.OFF) {
		// the Probe Status is off
		// so turn on this probe
		probeStatus = status;
		kickThreadOn();
	    } else {
		// it's already on, so nothing to do
	    }
	}
    }

    /**
     * Activate the probe
     */
    public ProbeLifecycle activateProbe() {
	startProbeThread();
	return this;
    }

    /**
     * Deactivate the probe
     */
    public ProbeLifecycle deactivateProbe() {
	stopProbeThread();
	return this;
    }

    /**
     * Activate the probe by starting the probe thread.
     */
    protected synchronized void startProbeThread() {
	if (!threadRunning) {
	    myThread = new Thread(this, getName());
	    threadRunning = true;
	    myThread.start();
	}
    }

    /**
     * Deactivate the probe by stopping the probe thread.
     */
    protected synchronized void stopProbeThread() {
	if (threadRunning) {
	    threadRunning = false;
	    myThread.interrupt();
	}
    }

    /**
     * To kick the thread on we do a notifyAll()
     * because it is in wait()
     */
    private synchronized void kickThreadOn() {
	notifyAll();
    }

    /**
     * To kick the thread off we interrupt it
     * because it is in sleep()
     */
    private synchronized void kickThreadOff() {
	myThread.interrupt();
    }

    /**
     * Collect a measurement for the reciever
     */
    public abstract ProbeMeasurement collect();

    /**
     * Inform the Probe of an object.
     * This turns the Probe on so it calls collect().
     */
    public Object inform(Object obj) {
    	informsToProcess.incrementAndGet();
	turnOnProbe();
	return obj;
    }

    /**
     * Collect a measurement and check its values against the attributes.
     */
    ProbeMeasurement collectThenCheck() {
	ProbeMeasurement m = collect();

	// check that the measurement
	// meets the spec
	try {
	    if (checkMeasurement(m)) {
		return m;
	    } else {
		return null;
	    }
	} catch (MeasurementException me) {
            error(me);
	    return null;
	}
    }

    /**
     * Called when there is an error in a Measurement.
     */
    public void error(MeasurementException me) {
        me.printStackTrace();
    }

    /**
     * The thread run() method
     */
    public void run() {
	//System.err.println("probe " + getName() + " running");

	// code to run at begining of thread
	beginThreadBody();

	while (threadRunning) {
	    if (probeStatus == Probe.Status.OFF) {
		// the probe is OFF
		// so wait

		try {
		    waitWhileOff();
		} catch (InterruptedException ie) {
		    System.err.println(getName() + ": InterruptedException");
		}

		//System.err.println(getName() + ": turned on");

	    } else {
		// the probe is ON

		/*
		 * collect the measurement
		 */
		ProbeMeasurement m = collectThenCheck();


		// if using events turn probe off
		// if there are no more events to process
		if (collectiontype ==  Probe.CollectionType.OnEvent) {
		    if ( informsToProcess.decrementAndGet() == 0 ) {
			setStatus(Probe.Status.OFF);
		    }
		}

		/*
		 * Report the measurement.
		 */
		if (m == null) {
		    // something went wrong or there is no measurement
		    // TODO: determine correct behaviour
		} else {
		    // set probe seq no
		    m.setSequenceNo(probeSeqNo);
		    probeSeqNo++;

		    // notify manager
		    if (reportingtype == Probe.ReportingType.AtDataRate) {
			manager.notifyMeasurement(m);
		    } else if (reportingtype == Probe.ReportingType.OnChange) {
			if (filter.filter(this, m)) {
			    manager.notifyMeasurement(m);
			}
		    } else {
			// reportingtype == Probe.ReportingType.OnRead
		    }

		    // save the last measurement
		    lastMeasurement = m;

		    // save the time of the measurement
                    if (lastMeasurement != null){
			lastMeasurementTime = m.getTimestamp();
                    } else {
			lastMeasurementTime = new Timestamp(System.currentTimeMillis());
                    }

		}


		/*
		 * Sleep until next time.
		 */
		if (collectiontype ==  Probe.CollectionType.AtDataRate) {
		    // Probe.CollectionType.AtDataRate
		    // work out the time to sleep.
		    // Note the dataRate can change at any time.
		    long timeToSleep = rationalToMillis(dataRate);

		    try {
			Thread.sleep(timeToSleep);
		    } catch (InterruptedException ie) {
			//System.err.println(getName() + ": turned off");
		    }
		}

		// end of Probe.Status == ON
	    }
	}

	// code to run at end of thread
	endThreadBody();

	System.err.println("exit thread loop for " + getName());
    }


    /**
     * The code to run at the begining of the thread body.
     * Used to set things up.
     */
    public void beginThreadBody() {}


    /**
     * The code to run at the end of the thread body.
     * Used to tidy things up.
     */
    public void endThreadBody() {}

    /**
     * The threads waits while the Probe.Status == OFF.
     */
    private synchronized void waitWhileOff() throws InterruptedException {
	if (probeStatus == Probe.Status.OFF) {
	    // wait
	    wait();
	} else {
	    // don;t wait
	}
    }

    /**
     * Convert the data rate to milliseconds
     */
    protected long rationalToMillis(Rational rational) {
	return (long)(rational.reciprocal().toDouble() * 3600 * 1000);
    }

    /**
     * To String
     */
    public String toString() {
	StringBuffer buffer = new StringBuffer();

	buffer.append("name:");
	buffer.append(getName());
	buffer.append("\t");
	buffer.append("dataRate:");
	buffer.append(dataRate);
	buffer.append("\t");
	buffer.append("lastMeasurementTime:");
	buffer.append(lastMeasurementTime);
	buffer.append("\t");
	buffer.append("active:");
	buffer.append(threadRunning);
	buffer.append("\t");
	buffer.append("on:");
	buffer.append(probeStatus.equals(Probe.Status.ON));
	buffer.append("\n");

	buffer.append("attributes:\n");

	for (ProbeAttribute attr : getAttributes()) {
	    buffer.append("\t- ");
	    buffer.append(attr);
	    buffer.append("\n");
	}
	
	return buffer.toString();

    }

}
