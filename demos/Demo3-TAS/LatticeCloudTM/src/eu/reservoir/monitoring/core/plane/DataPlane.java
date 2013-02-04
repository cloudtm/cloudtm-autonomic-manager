// DataPlane.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: May 2009

package eu.reservoir.monitoring.core.plane;

import eu.reservoir.monitoring.core.MeasurementReceiver;
import eu.reservoir.monitoring.core.DataSourceDelegateInteracter;

/**
 * A DataPlane.
 * This has the common methods for all data planes, although
 * some implementations may choose to only do dome of them.
 */
public interface DataPlane extends Plane, DataService, MeasurementReceiver {
}
