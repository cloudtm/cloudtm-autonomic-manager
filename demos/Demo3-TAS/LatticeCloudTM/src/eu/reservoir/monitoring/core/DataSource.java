// DataSource.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import java.util.Collection;
import java.util.Map;

/**
 * A data source
 */
public interface DataSource extends ProbeManager, PlaneInteracter {
    /**
     * Get the ID of the DataSource.
     */
    public ID getID();


    /**
     * Set the DataSource ID
     */
    public DataSource setID(ID id);

    /**
     * Get the name of the DataSource
     */
    public String getName();

    /**
     * Set the name of the DataSource
     */
    public DataSource setName(String name);

    /**
     * Add a new probe.
     */
    public DataSource addProbe(Probe p);

    /**
     * Remove a probe.
     */
    public DataSource removeProbe(Probe p);

    /**
     * Get a list of probe names.
     */
    public Collection<String> getProbeNames();

    /*
     * Get a list of probe IDs
     */
    public Collection<ID> getProbeIDs();

    /*
     * Get a list of probes.
     */
    public Collection<Probe> getProbes();

    /*
     * Get the Probe Meta Data for all the probes
     * Returns a Map using the Probe names as the keys, and
     * a map of probe attributes as the values.
     * e.g "memory" ->  [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String,  Map<String, Object> > getProbeMetaData();

    /*
     * Get the attributes for a Probe by name.
     */
    public Collection<ProbeAttribute> getProbeAttributes(String name);

    /*
     * Get the attributes for a Probe by ID
     */
    public Collection<ProbeAttribute> getProbeAttributes(ID probeID);

    /*
     * Get the attributes for a Probe.
     */
    public Collection<ProbeAttribute> getProbeAttributes(Probe p);

    /*
     * Get the Probe Meta Data for a specified Probe by name
     * Returns a map of probe attributes as the values.
     * e.g [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String, Object> getProbeMetaData(String name);

    /*
     * Get the Probe Meta Data for a specified Probe by ID
     * Returns a map of probe attributes as the values.
     * e.g [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String, Object> getProbeMetaData(ID probeID);

    /*
     * Get the Probe Meta Data for a specified Probe.
     * Returns a map of probe attributes as the values.
     * e.g [name: "memory", dataRate: 720/1, active: true, on: false]
     *
     */
    public Map<String, Object> getProbeMetaData(Probe p);

    /*
     * Get a probe by name.
     */
    public Probe  getProbeByName(String name);

    /*
     * Get a probe by ID.
     */
    public Probe  getProbeByID(ID probeID);

}
