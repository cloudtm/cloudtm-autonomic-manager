// DataSourceDelegateInteracter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Nov 2009

package eu.reservoir.monitoring.core;

/**
 * A DataSourceDelegateInteracter is responsible interacting with
 * a DataSource.
 */

public interface DataSourceDelegateInteracter {
    /**
     * Get the DataSourceDelegate
     */
    public DataSourceDelegate getDataSourceDelegate();

    /**
     * Set the DataSourceDelegate
     */
    public DataSourceDelegate setDataSourceDelegate(DataSourceDelegate ds);

}