// TableRow.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

import eu.reservoir.monitoring.core.TypeException;
import java.util.List;
import java.io.Serializable;

/**
 * An interface for rows of data that can be added to a Table.
 */
public interface TableRow extends Serializable {
    /**
     * Get the size of a row.
     * It should be the same size as the number of column 
     * definitions for the table that the row is in.
     */
    public int size();

    /**
     * Get the Nth element from a row.
     */
    public TableValue get(int n);

    /**
     * Set the Nth element of a row.
     */
    public TableRow set(int n, TableValue value);

    /**
     * Add some data value to the row.
     */
    public TableRow add(TableValue value);

    /**
     * Add an object value to the row.
     * This throws an Error if the type of the value is not
     * one supported by ProbeAttributeType.
     */
    public TableRow add(Object value) throws TypeException;

    /**
     * Convert the Row to a List.
     */
    public List<TableValue> toList();
}