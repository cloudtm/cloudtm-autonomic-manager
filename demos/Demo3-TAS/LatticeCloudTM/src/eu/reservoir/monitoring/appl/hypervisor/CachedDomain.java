// CachedDomain.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: July 2009

package eu.reservoir.monitoring.appl.hypervisor;

import org.libvirt.*;


/**
 * The cached data for a libvirt Domain.
 */
public class CachedDomain {
    // The id.
    int id;

    // the name of the domain
    String name;

    // the maximum amount of physical memory allocated to a domain.
    long maxMemory;

    //  the maximum number of virtual CPUs supported for the guest VM. 
    int	maxVcpus;

    // information about a domain.
    DomainInfo domainInfo;

    // network interface stats for interfaces attached to this domain.
    // DomainInterfaceStats domainIFStats;

    // block device (disk) stats for block devices attached to this domain.
    //DomainBlockStats domainBStats;

    // information about virtual CPUs of this domain
    VcpuInfo [] domainCpuInfo;

    /**
     * Get the ID.
     */
    public int getID() {
	return id;
    }

    /**
     * Get the name of a Domain.
     */
    public String getName() {
	return name;
    }

    /**
     * Get the maximum amount of memory for a  Domain.
     */
    public long getMaxMemory() {
	return maxMemory;
    }

    /**
     * Get the maximum no. of virtual CPUs for a  Domain.
     */
    public int getMaxVirtualCPUs() {
	return maxVcpus;
    }

    /**
     * Get the DomainInfo info for a Domain.
     */
    public DomainInfo getInfo() {
	return domainInfo;
    }

    /**
     * Get the VcpuInfo info for a Domain.
     */
    /*
    public VcpuInfo[] getVirtualCPUInfo() {
	return domainCpuInfo;
    }
    */
}