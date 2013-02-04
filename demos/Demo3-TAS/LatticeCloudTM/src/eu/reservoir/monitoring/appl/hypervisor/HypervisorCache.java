// HypervisorCache.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.hypervisor;

import java.io.*;
import java.util.LinkedList;
import java.util.HashMap;
import eu.reservoir.monitoring.appl.DynamicControl;
import org.libvirt.*;
import java.util.concurrent.Semaphore;
/*
 * TODO: add method to control sleep time.
 */

/**
 * This class caches data from a hypervisor.
 */
public class HypervisorCache extends DynamicControl {
    // The hypervisor
    Connect hypervisor;

    // the Hypervisor URI
    String hypervisorURI;

    // the start time
    long startTime = 0;

    // the int[] of domain ids
    int [] activeDoms = null;

    // a list of seen vees
    LinkedList<Integer> seenVEEs;
    
    // a list of Domains
    HashMap<Integer, CachedDomain> seenDomains = null;

    // A semaphore
    Semaphore semaphore;

    
    /**
     * Construct a HypervisorCache
     * and a hypervisor URI such as "xen:///"
     */
    public HypervisorCache(String hypervisorURI) {
	super("hypervisor_cache");
	this.hypervisorURI = hypervisorURI;
	semaphore = new Semaphore(1);
    }

    /**
     * Initialize this hypervisor cache
     * It connects to the hypervisor
     */
    protected void controlInitialize() {
	System.err.println("HypervisorCache: controlInitialize()");

	// sleep time is 1 sec
	setSleepTime(1); 

	startTime = System.currentTimeMillis();
	seenVEEs = new LinkedList<Integer>();
	seenDomains = new HashMap<Integer, CachedDomain>();

	// set up the hypervisor connection
	try {
	    hypervisor = new Connect(hypervisorURI);
	} catch (LibvirtException lve) {
	    throw new RuntimeException(lve);
	}

	// set the semaphore
	try {
	    semaphore.acquire();
	    System.err.println("HypervisorCache: acquire semaphore");
	    System.err.println("HypervisorCache: semaphore availablePermits = " + semaphore.availablePermits());

	} catch (InterruptedException ie) {
	    System.err.println("HypervisorCache: cound not acquire semaphore");
	}

    }


    /**
     * Actually evaluate something.
     */
    protected void controlEvaluate() {
	// Get the domain list from the hypervisor
	// This determines how many vees we have open
	// Each probe will do detailed analysis
	long now = System.currentTimeMillis();
	long diff = (now - startTime) / 1000;
	//System.err.println(diff + ": " + this + " seen " + seenVEEs.size());

	try {
	    // hypervisor
	    Connect libvirt = hypervisor;

	    // get the list of current VEEs
	    activeDoms = libvirt.listDomains();

	    LinkedList<Integer> seenThisTime = new LinkedList<Integer>();

	    // get the domain info
	    if (libvirt.numOfDomains() > 0) {
		for(int vee: activeDoms) {
		    Domain domain = libvirt.domainLookupByID(vee);

		    if (seenVEEs.contains(vee)) {
			// we've already seen this vee
			seenThisTime.add(vee);

			// update cached data
			CachedDomain cachedDomain = seenDomains.get(vee);


			getDomainData(domain, cachedDomain);

			
		    } else {
			// we've not seen this vee
			// so we cache it's domain
			seenThisTime.add(vee);

			// set cached data

			// create a CachedDomain
			CachedDomain cachedDomain = new CachedDomain();


			getDomainData(domain, cachedDomain);

			// it looks like domaininterfacestats and
			// domainblockstats need to be looked up on the fly
			// as there is no way to ask about the list of them


			// save in cache
			seenDomains.put(vee, cachedDomain);
		    }
		}
	    }


	    // we've got to the end, so now we determine
	    // if there are vees we have in the list that are
	    // not used any more
	    seenVEEs.removeAll(seenThisTime);

	    if (seenVEEs.size() == 0) {
		// there are no vees 
	    } else {
		// the are some residual vees
		// delete the domain cache for each one
		for (Integer aVEE : seenVEEs) {
		    seenDomains.remove(aVEE);
		}
	    }

	    // save the current list
	    seenVEEs = seenThisTime;


	} catch (LibvirtException lve) {
	    System.err.println("LibvirtException " + lve + " in HypervisorCache");
	}


	// if this has the semaphore, release it
	if (semaphore.availablePermits() == 0) {
	    semaphore.release();
	    System.err.println("HypervisorCache: controlEvaluate() semaphore released");
	}
    }

    /**
     * Wait for the cache to be ready.
     * This is after the first read of the Hypervisor info.
     */
    public void waitForCacheReady() {
	System.err.println("HypervisorCache: waitForCacheReady() semaphore availablePermits = " + semaphore.availablePermits());
	if (semaphore.availablePermits() == 0) {
	    try {
		System.err.println("HypervisorCache: about to acquire()");
		semaphore.acquire();
		System.err.println("HypervisorCache: acquired");
		semaphore.release();
		System.err.println("HypervisorCache: release");
	    } catch (InterruptedException ie) {
	    }
	}
    }

    /**
     * Cleanup
     */
    protected void controlCleanup() {
	try {
	    // disconnect
	    hypervisor.close();
	} catch (LibvirtException lve) {
	    throw new RuntimeException(lve);
	}
   }

    /**
     * Get cached domain data.
     */
    protected void getDomainData(Domain domain, CachedDomain cachedDomain) throws LibvirtException {
	cachedDomain.id = domain.getID();
	cachedDomain.name = domain.getName();
	cachedDomain.maxMemory = domain.getMaxMemory();
	cachedDomain.maxVcpus = domain.getMaxVcpus();
	cachedDomain.domainInfo = domain.getInfo();
	//cachedDomain.domainCpuInfo = domain.getVcpusInfo();
    }

    /*
     * External interface.
     */

    /**
     * Get the list of domain ids.
     */
    public int[] listDomains() {
	return activeDoms;
    }

    /**
     * Get the number of domains
     */
    public int numOfDomains() {
	if (activeDoms == null) {
	    return 0;
	} else {
	    return activeDoms.length;
	}
    }

    /**
     * Get a domain by it's id.
     */
    public CachedDomain domainLookupByID(int id) {
	return seenDomains.get(id);
    }

    /**
     * Main entry point.
     */
    public static void main(String[] args) throws Exception {
	HypervisorCache cache = new HypervisorCache("xen:///");

	// activate the  control
	cache.activateControl();


	for (int x=0; x <1000; x++) {
	    // sleep a bit
	    try {
		Thread.sleep(5000);
	    } catch (InterruptedException ie) {
	    }

	    // get the list of current VEEs
	    int[] activeDoms = cache.listDomains();

	    // get the domain info
	    if (cache.numOfDomains() > 0) {
		for(int d: activeDoms) {
		    CachedDomain cached = cache.domainLookupByID(d);
		    String veeName = cached.getName();
		    int id = cached.getID();
		    long mem = cached.getInfo().memory/1024;
		    int vCPUs = cached.getInfo().nrVirtCpu;
		    int state = HypervisorState.processState(cached.getInfo().state);
		    double time = cached.getInfo().cpuTime / (1000 * 1000 * 1000);

		    System.err.printf("%-25s%5d%5d%5d%5c%10.2f\n", (veeName + ":"), d, mem, vCPUs, state, time);
		}
	    }

	}
    }


}
