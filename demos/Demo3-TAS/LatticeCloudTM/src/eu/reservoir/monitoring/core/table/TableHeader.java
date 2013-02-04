// TableHeader.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

import eu.reservoir.monitoring.core.ProbeAttributeType;
import java.util.List;
import java.io.Serializable;

/**
 * An interface for the header of a Table.
 */
public interface TableHeader extends Serializable {
    /**
     * Get the number of columns.
     */
    public int size();

    /**
     * Get the Nth element from the header.
     */
    public TableAttribute get(int n);

    /**
     * Set the Nth element of a row.
     */
    public TableHeader set(int n, TableAttribute value);

    /**
     * Add a column definition to the header.
     */
    public TableHeader add(String name, ProbeAttributeType type);

    /**
     * Convert the TableHeader to a List.
     */
    public List<TableAttribute> toList();


}