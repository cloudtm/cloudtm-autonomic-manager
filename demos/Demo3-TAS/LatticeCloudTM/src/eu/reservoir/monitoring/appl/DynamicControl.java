// DynamicControl.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.appl;

import eu.reservoir.monitoring.core.*;

/**
 * A Dynamic Control.
 * This runs in its own Thread, and allows subclasses to provide
 * initialize, cleanup, and an evaluation function.
 */
public abstract class DynamicControl  implements Runnable {
    /**
     * The thread that this control runs in
     */
    Thread myThread = null;

    /**
     * Thread running?
     */
    boolean threadRunning = false;

    String name = null;

    // The sleep amount, in seconds
    int sleepTime = 0;

    // Total elapsed time
    int elapsedTime = 0;

    /*
     * Create a Dynamic Control.
     */
    public DynamicControl(String name) {
	this.name = name;
	setSleepTime(10);
    }

    /**
     * The name
     */
    public String getName() {
	return name;
    }

    /**
     * Get the sleep time between activations.
     * This is in seconds.
     */
    public int getSleepTime() {
	return sleepTime;
    }

    /**
     * Set the sleep time between activations.
     * This is in seconds.
     */
    public DynamicControl setSleepTime(int sl) {
	sleepTime = sl;
	return this;
    }

    /**
     * Get the amount of elapsedTime.
     * This is in milliseconds.
     */
    public int getElapsedTime() {
	return elapsedTime;
    }

    /**
     * The run method of the Runnable, which wakes up from time
     * to time and determines if a Probe should be created or 
     * destroyed.
     */
    public void run() {
		//System.err.println("dynamiccontrol " + getName() + " running");
	
		// initialize the process
		controlInitialize();
		boolean interrupting = false;
		//while (threadRunning) {
		while (!interrupting) {
		    // check for work
		    try {
				// evaluate the control mechanism
				controlEvaluate();
				// now sleep
				sleep();
		    } catch (InterruptedException ie) {
		    	System.err.println(getName() + ": turned off");
		    }
		}
	
		// cleanup the process
		controlCleanup();
	
		System.err.println("exit thread loop for " + getName());
    }

    /**
     * Actualy sleep.
     */
    protected void sleep() throws InterruptedException {
	// work out the time to sleep.
	long timeToSleep = sleepTime*1000;  // N seconds

	// do the sleep
	Thread.sleep(timeToSleep);

	elapsedTime += timeToSleep;
    }
    
    /**
     * Initialize.
     */
    protected abstract void controlInitialize();

    /**
     * Actually evaluate something.
     */
    protected abstract void controlEvaluate();

    /**
     * Cleanup
     */
    protected abstract void controlCleanup();

    /**
     * Activate the DynamicControl thread.
     */
    synchronized public void activateControl() {
	if (!threadRunning) {
	    myThread = new Thread(this);
	    myThread.start();
	    threadRunning = true;
	}
    }

    /**
     * Deactivate the DynamicControl thread.
     */
    synchronized public void deactivateControl() {
	if (threadRunning) {
	    threadRunning = false;
	    myThread.interrupt();
	}
    }

    /**
     * Is the Thread running
     */
    public boolean isRunning() {
	return threadRunning;
    }


}
