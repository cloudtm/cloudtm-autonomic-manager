// AbstractDataSource.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import eu.reservoir.monitoring.core.plane.*;

/**
 * An abstract Data Source.
 * It receives measurements from the probes and it also reports
 * measurements to the delegate object.
 */
public abstract class AbstractDataSource implements DataSource, PlaneInteracter, DataSourceDelegateInteracter, Runnable {
    /*
     * The ID
     */
    ID myID;

    /*
     * The data source name
     */
    String name = "data-source";

    /*
     * The sequence no for outgoing measurements.
     */
    int seqNo = 0;

    /*
     * The queue of measurements that have been received from the Probes.
     * They measurements are queued up and then send to the data plane in
     * this thread.
     * By using a BlockingQueue we get locking and synchronization built-in,
     * and saves having to build it ourselves.
     */
    LinkedBlockingQueue measurementQueue;

    /*
     * A map of probe names to probes.
     */ 
    Map<String, Probe> probeMap = new HashMap<String,Probe>();

    /*
     * A map of probe IDs to probes
     */
    Map<ID, Probe> probeIDMap = new HashMap<ID, Probe>();

    /*
     * The receiver of measurements from this DataSource
     */
    DataSourceDelegate dataSourceDelegate = null;


    /*
     * My Thread.
     */
    Thread myThread = null;

    /*
     * Thread running?
     */
    boolean threadRunning = false;


    /**
     * Construct a DataSource.
     */
    public AbstractDataSource() {
	// set the ID of this DataSource
	myID = new ID(this.hashCode());
	// set up a default DataSourceDelegate
	setDataSourceDelegate(new DefaultDataSourceDelegate(this));

	// set up queue
	measurementQueue = new LinkedBlockingQueue();

	// start QueueHandling
	startQueueHandlingThread();

    }

    /**
     * Activate the transmission from the queue to the data plane by starting the thread.
     */
    protected synchronized void startQueueHandlingThread() {
	if (!threadRunning) {
	    myThread = new Thread(this, getName());
	    threadRunning = true;
	    myThread.start();
	}
    }

    /**
     * Deactivate the transmission from the queue to the data plane by stopping the thread.
     */
    protected synchronized void stopQueueHandlingThread() {
	if (threadRunning) {
	    threadRunning = false;
	    myThread.interrupt();
	}
    }

    /**
     * Add a new probe.
     */
    public DataSource addProbe(Probe p) {
	registerProbe(p);
	activateProbe(p);

	// Tell InfoPlane about this probe
	dataSourceDelegate.addProbeInfo(p);

	return this;
    }    

    /**
     * Remove a probe.
     */
    public DataSource removeProbe(Probe p) {
	// Remove probe from InfoPlane
	dataSourceDelegate.removeProbeInfo(p);

	deactivateProbe(p);
	unregisterProbe(p);
	return this;
    }

    /**
     * Register a probe
     */
    protected void registerProbe(Probe probe) {
	// get its name
	String name = probe.getName();
	// get its id
	ID probeID = probe.getID();
	// set this as it's ProbeManger
	probe.setProbeManager((ProbeManager)this);
	// add the Probe to the probe map
	probeMap.put(name, probe);
	probeIDMap.put(probeID, probe);
    }

    /**
     * Unregister a probe
     */
    protected void unregisterProbe(Probe probe) {
	// get its name
	String name = probe.getName();
	// get its id
	ID probeID = probe.getID();
	// clear it's ProbeManger
	probe.setProbeManager(null);
	// remove the Probe to the probe map
	probeMap.remove(name);
	probeIDMap.remove(probeID);
    }

    /**
     * Get the Probe ID
     */
    public ID getID() {
	return myID;
    }


    /**
     * Set the DataSource ID
     */
    public DataSource setID(ID id) {
	myID = id;
	return this;
    }

    /**
     * Get the name of the DataSource
     */
    public String getName() {
	return name;
    }

    /**
     * Set the name of the DataSource
     */
    public DataSource setName(String name) {
	this.name = name;
	return this;
    }


    /**
     * Get a list of probe names.
     */
    public Collection<String> getProbeNames() {
	return probeMap.keySet();
    }

    /**
     * Get a list of probe IDs
     */
    public Collection<ID> getProbeIDs() {
	return probeIDMap.keySet();
    }

    /**
     * Get a list of probes.
     */
    public Collection<Probe> getProbes() {
	return probeMap.values();
    }

    /**
     * Get the attributes for a Probe by name.
     */
    public Collection<ProbeAttribute> getProbeAttributes(String name) {
	Probe p = probeMap.get(name);
	return ((p != null) ? getProbeAttributes(p) : null);
    }

    /**
     * Get the attributes for a Probe by ID.
     */
    public Collection<ProbeAttribute> getProbeAttributes(ID probeID) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? getProbeAttributes(p) : null);
    }

    /**
     * Get the attributes for a Probe.
     */
    public Collection<ProbeAttribute> getProbeAttributes(Probe p) {
	return p.getAttributes();
    }

    /**
     * Get the Probe Meta Data for all the probes
     * Returns a Map using the Probe names as the keys, and
     * a map of probe attributes as the values.
     * e.g "memory" ->  [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String,  Map<String, Object> > getProbeMetaData() {
	Map<String,  Map<String, Object> > result = new HashMap<String,  Map<String, Object> >();

	// visit each key of the probeMap and get its meta data
	// in groovy: probeMap.each { k,v -> result += [(k) : v.getMetaData() ] }
      
	for (Map.Entry<String, Probe> entry : probeMap.entrySet()) {
	    result.put(entry.getKey(), ((Probe)entry.getValue()).getMetaData());
	}

	return result;
    }

    /**
     * Get the Probe Meta Data for a specified Probe by name
     * Returns a map of probe attributes as the values.
     * e.g [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String, Object> getProbeMetaData(String name) {
	Probe p = probeMap.get(name);
	return ((p != null) ? getProbeMetaData(p) : null);
    }

    /**
     * Get the Probe Meta Data for a specified Probe by ID
     * Returns a map of probe attributes as the values.
     * e.g [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String, Object> getProbeMetaData(ID probeID) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? getProbeMetaData(p) : null);
    }

    /**
     * Get the Probe Meta Data for a specified Probe.
     * Returns a map of probe attributes as the values.
     * e.g [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String, Object> getProbeMetaData(Probe p) {
	return p.getMetaData();
    }


    /**
     * Get a probe by name.
     * @return the Probe with name name, null otherwise
     */
    public Probe getProbeByName(String name) {
	return probeMap.get(name);
    }

    /**
     * Get a probe by ID
     * @return the Probe with ID, null otherwise
     */
    public Probe getProbeByID(ID probeID) {
	return probeIDMap.get(probeID);
    }

    /*
     * Probe Lifecycle stuff
     */


    /**
     * Turn on a Probe by name
     */
    public DataSource turnOnProbe(String name) {
	Probe p = probeMap.get(name);
	return ((p != null) ? turnOnProbe(p) : null);
    }

    /**
     * Turn on a Probe by ID
     */
    public DataSource turnOnProbe(ID probeID) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? turnOnProbe(p) : null);
    }

    /**
     * Turn on a Probe
     */
    public DataSource turnOnProbe(Probe p) {
	p.turnOnProbe();
	return this;
    }

    /**
     * Turn off a Probe by name
     */
    public DataSource turnOffProbe(String name) {
	Probe p = probeMap.get(name);
	return ((p != null) ? turnOffProbe(p) : null);
    }

    /**
     * Turn off a Probe by ID
     */
    public DataSource turnOffProbe(ID probeID) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? turnOffProbe(p) : null);
    }

    /**
     * Turn off a Probe
     */
    public DataSource turnOffProbe(Probe p) {
	p.turnOffProbe();
	return this;
    }

    /**
     * Is this Probe turned on by name
     * The thread is running, but is the Probe getting values.
     */
    public boolean isProbeOn(String name) {
	Probe p = probeMap.get(name);
	return ((p != null) ? isProbeOn(p) : false);
    }

    /**
     * Is this Probe turned on by ID
     * The thread is running, but is the Probe getting values.
     */
    public boolean isProbeOn(ID probeID)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? isProbeOn(p) : false);
    }


    /**
     * Is this Probe turned on.
     * The thread is running, but is the Probe getting values.
     */
    public boolean isProbeOn(Probe p) {
	return p.isOn();
    }

    /**
     * Activate a Probe by name
     */
    public DataSource activateProbe(String name) {
	Probe p = probeMap.get(name);
	return ((p != null) ? activateProbe(p) : null);
    }

    /**
     * Activate a Probe by ID
     */
    public DataSource activateProbe(ID probeID) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? activateProbe(p) : null);
    }

    /**
     * Activate a Probe
     */
    public DataSource activateProbe(Probe p) {
	p.activateProbe();
	return this;
    }

    /**
     * Deactivate a Probe by name
     */
    public DataSource deactivateProbe(String name) {
	Probe p = probeMap.get(name);
	return ((p != null) ? deactivateProbe(p) : null);
    }

    /**
     * Deactivate a Probe by ID
     */
    public DataSource deactivateProbe(ID probeID) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? deactivateProbe(p) : null);
    }

    /**
     * Deactivate a Probe
     */
    public DataSource deactivateProbe(Probe p) {
	if (p.isActive()) {
	    p.deactivateProbe();
	} else {
	    // nothing to do
	}
	return this;
    }

    /**
     * Has this probe been activated by name
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isProbeActive(String name)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? isProbeActive(p) : false);
    }



    /**
     * Has this probe been activated by ID
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isProbeActive(ID probeID) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? isProbeActive(p) : null);
    }



    /**
     * Has this probe been activated.
     * Is the thread associated with a Probe acutally running. 
     */
    public boolean isProbeActive(Probe p) {
	return p.isActive();
    }


    /*
     * Probe interaction stuff
     */

    /**
     * Get the name of the Probe by name
     */
    public String getProbeName(String name)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? getProbeName(p) : null);
    }

    /**
     * Get the name of the Probe by ID
     */
    public String getProbeName(ID probeID)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? getProbeName(p) : null);
    }

    /**
     * Get the name of the Probe
     */
    public String getProbeName(Probe p) {
	return p.getName();
    }

    /**
     * Set the name of the Probe by name
     */
    public boolean setProbeName(String name, String newName)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? setProbeName(p, newName) : false);
    }

    /**
     * Set the name of the Probe by ID
     */
    public boolean setProbeName(ID probeID, String newName)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? setProbeName(p, newName) : false);
    }

    /**
     * Set the name of the Probe
     */
    public boolean setProbeName(Probe p, String newName) {
	p.setName(newName);
	return true;
    }

    /**
     * Get the Service ID of the Probe by name
     */
    public ID getProbeServiceID(String name)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? getProbeServiceID(p) : null);
    }

    /**
     * Get the Service ID of the Probe by ID
     */
    public ID getProbeServiceID(ID probeID)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? getProbeServiceID(p) : null);
    }

    /**
     * Get the Service ID of the Probe.
     */
    public ID getProbeServiceID(Probe p) {
	return p.getServiceID();
    }

    /**
     * Set the Service ID for a Probe by name
     */
    public boolean setProbeServiceID(String name, ID id)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? setProbeServiceID(p, id) : false);
    }

    /**
     * Set the Service ID for a Probe by ID
     */
    public boolean setProbeServiceID(ID probeID, ID id)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? setProbeServiceID(p, id) : false);
    }


    /**
     * Set the Service ID for a Probe
     */
    public boolean setProbeServiceID(Probe p, ID id) {
	p.setServiceID(id);
	return true;
    }

    /**
     * Get the Group ID of the Probe by name
     */
    public ID getProbeGroupID(String name)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? getProbeGroupID(p) : null);
    }

    /**
     * Get the Group ID of the Probe by ID
     */
    public ID getProbeGroupID(ID probeID)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? getProbeGroupID(p) : null);
    }

    /**
     * Get the Group ID of the Probe.
     */
    public ID getProbeGroupID(Probe p) {
	return p.getGroupID();
    }

    /**
     * Set the Group ID for a Probe by name
     */
    public boolean setProbeGroupID(String name, ID id)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? setProbeGroupID(p, id) : false);
    }

    /**
     * Set the Group ID for a Probe by ID
     */
    public boolean setProbeGroupID(ID probeID, ID id)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? setProbeGroupID(p, id) : false);
    }


    /**
     * Set the Group ID for a Probe
     */
    public boolean setProbeGroupID(Probe p, ID id) {
	p.setGroupID(id);
	return true;
    }

    /**
     * Get the data rate for a Probe by name
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getProbeDataRate(String name) {
	Probe p = probeMap.get(name);
	return ((p != null) ? getProbeDataRate(p) : null);
    }

    /**
     * Get the data rate for a Probe by ID
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getProbeDataRate(ID probeID) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? getProbeDataRate(p) : null);
    }


    /**
     * Get the data rate for a Probe
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public Rational getProbeDataRate(Probe p) {
	return p.getDataRate();
    }

    /**
     * Set the data rate for a Probe by name
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public DataSource setProbeDataRate(String name, Rational dataRate) {
	Probe p = probeMap.get(name);
	return ((p != null) ? setProbeDataRate(p, dataRate) : null);
    }

    /**
     * Set the data rate for a Probe by ID
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public DataSource setProbeDataRate(ID probeID, Rational dataRate) {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? setProbeDataRate(p, dataRate) : null);
    }


    /**
     * Set the data rate for a Probe
     * The data rate is a Rational.
     * Specified in measurements per hour
     */
    public DataSource setProbeDataRate(Probe p, Rational dataRate) {
	p.setDataRate(dataRate);
	return this;
    }


    /**
     * Get the last measurement that was collected by name
     */
    public Measurement getProbeLastMeasurement(String name)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? getProbeLastMeasurement(p) : null);
    }


    /**
     * Get the last measurement that was collected by ID
     */
    public Measurement getProbeLastMeasurement(ID probeID)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? getProbeLastMeasurement(p) : null);
    }

    /**
     * Get the last measurement that was collected
     */
    public Measurement getProbeLastMeasurement(Probe p)  {
	return p.getLastMeasurement();
    }


    /**
     * Get the last time a measurement was collected by name
     */
    public Timestamp getProbeLastMeasurementCollection(String name)  {
	Probe p = probeMap.get(name);
	return ((p != null) ? getProbeLastMeasurementCollection(p) : null);
    }

    /**
     * Get the last time a measurement was collected by ID.
     */
    public Timestamp getProbeLastMeasurementCollection(ID probeID)  {
	Probe p = probeIDMap.get(probeID);
	return ((p != null) ? getProbeLastMeasurementCollection(p) : null);
    }


    /**
     * Get the last time a measurement was collected
     */
    public Timestamp getProbeLastMeasurementCollection(Probe p)  {
	return p.getLastMeasurementCollection();
    }




    /*
     * Probe  -> ProbeManager notification
     */


    /*
     * TODO:  this should run it its own thread so that
     * the probes can return immediately
     * and let this deal with transmission to the network layer
     * independently of the probes.
     */

    /**
     * Recieve a measurment from the Probe
     * and pass it onto the data source delegate.
     * @return -1 if something goes wrong
     * @return 0 if there is no delegate or no data plane
     */
    public int notifyMeasurement(Measurement m) {
	//System.err.println("DataSource: " + name + ": " + m);

	try {
	    // add the Measurement to the queue
	    //System.err.println("+" + seqNo + "/" + m.getProbeID() + "." + m.getSequenceNo() + " <> " + measurementQueue.size() );
	    measurementQueue.put(m);

	    // increase seqNo for next message
	    seqNo++;

	    return 1;
	} catch (InterruptedException ie) {
	    System.err.println("Can't add Measurement " + m + " to queue");
	    return 0;
	}
    }

    /**
     * The thread body.
     * It sends stuff from the queue onto the network.
     */
    public void run() {
	// code to run at begining of thread
	beginThreadBody();

	while (threadRunning) {
	    
	    // get Measurement off queue
	    // we know here it is a ProbeMeasurement
	    ProbeMeasurement m = null;

	    try {
		// by doing a take() this waits for the queue have
		// something in it. this means we don't have to build
		// our own locking and synchronization mechanism
		m = (ProbeMeasurement)measurementQueue.take();
		//System.err.println("-" + m.getProbeID() + "." + m.getSequenceNo());
	    } catch (InterruptedException ie) {
		System.err.println("Can't take Measurement " + m + " from queue");
		// loop round
		continue;
	    }
    
	    // now send it
	    try {
		DataPlaneMessage msg = measurementMessage((ProbeMeasurement)m);
		int retVal = dataSourceDelegate.sendData(msg);

	    } catch (Exception ex) {
		System.err.println("Failed to send Measurement: " + m);
		ex.printStackTrace();
	    }
	}

 	// code to run at end of thread
	endThreadBody();

	System.err.println("exit thread loop for " + getName());
   }

    /**
     * Create a DataPlaneMessage message from a Measurement.
     * The measurement is wrapped up as a DataPlaneMessage message
     * in order that we can add more data and get the Plane
     * object to demux multiple requests.
     */
    protected DataPlaneMessage measurementMessage(ProbeMeasurement m) throws IOException {
	DataPlaneMessage msg = new MeasurementMessage(m);
	msg.setDataSource(this);
	msg.setSeqNo(seqNo);

	return msg;
    }



    /**
     * Get the delegate that will recieve the measurments.
     */
    public DataSourceDelegate getDataSourceDelegate() {
	return dataSourceDelegate;
    }

    /**
     * Set the delegate that will recieve the measurments.
     */
    public DataSourceDelegate setDataSourceDelegate(DataSourceDelegate dsd) {
	dataSourceDelegate = dsd;
	return dataSourceDelegate;
    }

    /**
     * Get the DataPlane this is a delegate for.
     */
    public DataPlane getDataPlane() {
	return dataSourceDelegate.getDataPlane();
    }

    /**
     * Set the DataPlane this is a delegate for.
     */
    public PlaneInteracter setDataPlane(DataPlane dataPlane) {
	dataSourceDelegate.setDataPlane(dataPlane);
	return this;
    }

    /**
     * Get the ControlPlane this is a delegate for.
     */
    public ControlPlane getControlPlane() {
	return dataSourceDelegate.getControlPlane();
    }

    /**
     * Set the ControlPlane this is a delegate for.
     */
    public PlaneInteracter setControlPlane(ControlPlane controlPlane) {
	dataSourceDelegate.setControlPlane(controlPlane);
	return this;
    }

    /**
     * Get the InfoPlane this is a delegate for.
     */
    public InfoPlane getInfoPlane() {
	return dataSourceDelegate.getInfoPlane();
    }

    /**
     * Set the InfoPlane this is a delegate for.
     */
    public PlaneInteracter setInfoPlane(InfoPlane infoPlane) {
	dataSourceDelegate.setInfoPlane(infoPlane);
	return this;
    }

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect() {
	return dataSourceDelegate.connect();
    }

    /**
     * Is this connected to a delivery mechansim.
     */
    public boolean isConnected() {
	return dataSourceDelegate.isConnected();
    }

    /**
     * Dicconnect from a delivery mechansim.
     */
    public boolean disconnect() {
	return dataSourceDelegate.disconnect();
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
     * To String
     */
    public String toString() {
	return "DataSource: " + name + " for " + probeMap.keySet();
    }

}
