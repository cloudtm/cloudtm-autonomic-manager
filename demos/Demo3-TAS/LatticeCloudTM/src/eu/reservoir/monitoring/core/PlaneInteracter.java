// PlaneInteracter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.core;

import eu.reservoir.monitoring.core.plane.*;
import java.io.IOException;

/**
 * A PlaneInteracter has the methods to interact with Planes.
 */
public interface PlaneInteracter {
    /**
     * Get the DataPlane this is a delegate for.
     */
    public DataPlane getDataPlane();

    /**
     * Set the DataPlane this is a delegate for.
     */
    public PlaneInteracter setDataPlane(DataPlane dataPlane);

    /**
     * Get the ControlPlane this is a delegate for.
     */
    public ControlPlane getControlPlane();

    /**
     * Set the ControlPlane this is a delegate for.
     */
    public PlaneInteracter setControlPlane(ControlPlane controlPlane);

    /**
     * Get the InfoPlane this is a delegate for.
     */
    public InfoPlane getInfoPlane();

    /**
     * Set the InfoPlane this is a delegate for.
     */
    public PlaneInteracter setInfoPlane(InfoPlane infoPlane);

    /**
     * Connect to a delivery mechansim.
     */
    public boolean connect();

    /**
     * Is the PlaneInteracter connected to a delivery mechansim.
     */
    public boolean isConnected();

    /**
     * Dicconnect from a delivery mechansim.
     */
    public boolean disconnect();
}