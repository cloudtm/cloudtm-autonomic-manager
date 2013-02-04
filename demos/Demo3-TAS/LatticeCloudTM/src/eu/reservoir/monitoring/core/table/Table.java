// Table.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

import eu.reservoir.monitoring.core.TypeException;
import java.util.List;
import java.io.Serializable;

/**
 * An interface for a Table that can be used as a ProbeAttributeType.
 */
public interface Table extends Serializable {
    /**
     * Define all columns
     */
    public Table defineTable(TableHeader header);

    /**
     * Define all columns.
     */
    public Table defineTable(List<TableAttribute> attrs);

    /**
     * Add a row to the table.
     */
    public Table addRow(TableRow row) throws TableException, TypeException;

    /**
     * Add a row to the table.
     * Pass in a lit of TableValue.
     */
    public Table addRow(List<TableValue> row) throws TableException, TypeException;

    /**
     * Get a row.
     */
    public TableRow getRow(int rowNo);

    /**
     * Get the number of columns in the table.
     */
    public int getColumnCount();

    /**
     * Get the number of rows in the table.
     */
    public int getRowCount();


    /**
     * Get the list of all the column definitions.
     */
    public TableHeader getColumnDefinitions();

    /**
     * Convert the Table to a List of TableRows.
     */
    public List<TableRow> toList();

}