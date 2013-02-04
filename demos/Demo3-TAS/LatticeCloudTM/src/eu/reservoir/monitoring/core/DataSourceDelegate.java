// DataSourceDelegate.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package eu.reservoir.monitoring.core;

import eu.reservoir.monitoring.core.plane.*;
import java.io.IOException;

/**
 * A DataSourceDelegate is responsible interacting with
 * the data plane, the info plane, and the control plane on behalf of
 * a DataSource.
 * It's role is to insulate the DataSource and the Probes
 * from the real implementations of the Planes.
 */
public interface DataSourceDelegate extends DataSourceInteracter, PlaneInteracter, DataService, InfoService, ControlService {

}