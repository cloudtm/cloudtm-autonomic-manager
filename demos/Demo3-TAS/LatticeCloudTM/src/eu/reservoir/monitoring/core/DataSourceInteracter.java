// DataSourceInteracter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.reservoir.monitoring.core;

/**
 * A DataSourceInteracter is responsible interacting with
 * a DataSource.
 */

public interface DataSourceInteracter {
    /**
     * Get the DataSource
     */
    public DataSource getDataSource();

    /**
     * Set the DataSource
     */
    public DataSource setDataSource(DataSource ds);

}