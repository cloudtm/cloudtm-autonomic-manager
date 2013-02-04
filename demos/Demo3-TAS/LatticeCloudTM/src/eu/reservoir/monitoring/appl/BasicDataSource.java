// BasicDataSource.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.appl;

import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.core.plane.*;
import java.io.IOException;

/**
 * A BasicDataSource is a DataSource object that is used in application 
 * level code.  It has the necessary functionality to act as a DataSource
 * and have plugins for each of the data plane, control plane, and
 * info plane.
 * <p>
 * The BasicDataSource is built from an AbstractDataSource and a
 * DefaultDataSourceDelegate.  It has no special functionality over
 * and above that provided in the core implementation.
 * Clearly, subclasses can extend the standard behaviour.
 */
public class BasicDataSource extends AbstractDataSource {
    /**
     * Construct a BasicDataSource with a system generated name.
     * This uses the DefaultDataSourceDelegate to interact with the planes.
     */
    public BasicDataSource() {
	super();
	setName(this.getClass().getName() + "." + System.identityHashCode(this));

	// The DataSourceDelegate for interacting with the planes.
	DataSourceDelegate delegate = new DefaultDataSourceDelegate(this);
	setDataSourceDelegate(delegate);
    }

    /**
     * Construct a BasicDataSource with a name.
     * This uses the DefaultDataSourceDelegate to interact with the planes.
     */
    public BasicDataSource(String name) {
	super();
	setName(name);

	// The DataSourceDelegate for interacting with the planes.
	DataSourceDelegate delegate = new DefaultDataSourceDelegate(this);
	setDataSourceDelegate(delegate);
    }

    /**
     * Construct a BasicDataSource with a name and a DataSourceDelegate.
     */
    protected BasicDataSource(String name, DataSourceDelegate delegate) {
	super();
	setName(name);

	// The DataSourceDelegate for interacting with the planes is in arg
	setDataSourceDelegate(delegate);
    }

}
