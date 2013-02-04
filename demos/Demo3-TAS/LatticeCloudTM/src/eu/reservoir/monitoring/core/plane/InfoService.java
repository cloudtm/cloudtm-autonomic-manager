// InfoService.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package eu.reservoir.monitoring.core.plane;

import eu.reservoir.monitoring.core.*;
import java.io.Serializable;

/**
 * The InfoService
 * This has the common methods for getting info.
 */
public interface InfoService {
    /*
     * Look up things.
     */

    /**
     * Lookup some info in the InfoPlane.
     */
    public Object lookupDataSourceInfo(DataSource dataSource, String info);

    /**
     * Lookup some info in the InfoPlane.
     * Mostly used at the management end, as it uses DataSource ID.
     */
    public Object lookupDataSourceInfo(ID dataSourceID, String info);

    /**
     * Lookup some info in the InfoPlane.
     */
    public Object lookupProbeInfo(Probe probe, String info);

    /**
     * Lookup some info in the InfoPlane.
     * Mostly used at the management end, as it uses Probe ID.
     */
    public Object lookupProbeInfo(ID probeID, String info);

    /**
     * Lookup some info in the InfoPlane.
     */
    public Object lookupProbeAttributeInfo(Probe probe, int field, String info);

    /**
     * Lookup some info in the InfoPlane.
     * Mostly used at the management end, as it uses Probe ID.
     */
    public Object lookupProbeAttributeInfo(ID probeID, int field, String info);

    /*
     * Add things
     */

    /**
     * Add DataSource info.
     */
    public boolean addDataSourceInfo(DataSource ds);

    /**
     * Add Probe info.
     */
    public boolean addProbeInfo(Probe p);

    /**
     * Add ProbeAttribute Info to a Probe Info.
     */
    public boolean addProbeAttributeInfo(Probe p, ProbeAttribute pa);

    /*
     * Modify things
     */

    /**
     * Modify DataSource Info.
     */
    public boolean modifyDataSourceInfo(DataSource ds);

    /**
     * Modify Probe Info.
     */
    public boolean modifyProbeInfo(Probe p);

    /**
     * Modify  ProbeAttribute Info for a Probe.
     */
    public boolean modifyProbeAttributeInfo(Probe p, ProbeAttribute pa);

    /*
     * Remove things
     */

    /**
     * Remove DataSource Info.
     */
    public boolean removeDataSourceInfo(DataSource ds);

    /**
     * Remove Probe Info.
     */
    public boolean removeProbeInfo(Probe p);

    /**
     * Remove ProbeAttribute Info for a Probe.
     */
    public boolean removeProbeAttributeInfo(Probe p, ProbeAttribute pa);

    /*
     * General .
     */

    /**
     * Put a value in the InfoPlane.
     */
    public boolean putInfo(String key, Serializable value);

    /**
     * Get a value from the InfoPlane.
     */
    public Object getInfo(String key);

    /**
     * Remove a value from the InfoPlane.
     */
    public boolean removeInfo(String key);

}