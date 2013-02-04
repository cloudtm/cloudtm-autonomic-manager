// DefaultTable.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.monitoring.core.table;

import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.TypeException;
import java.util.List;
import java.util.LinkedList;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;


/**
 * A default implementation of a Table.
 */
public class DefaultTable implements Table {
    // The list of table column definitions
    TableHeader columnDefs;

    // A list of TableRows that are part of this table
    List<TableRow> rows;

    /**
     * Construct a Table.
     */
    public DefaultTable() {
	columnDefs = null;
	rows = new LinkedList<TableRow>();
    }

    /**
     * Define all columns
     */
    public Table defineTable(TableHeader header) {
	columnDefs = header;
	return this;
    }

    /**
     * Define all columns.
     */
    public Table defineTable(List<TableAttribute> attrs) {
	TableHeader header = new DefaultTableHeader();

	for (int a=0; a < attrs.size(); a++) {
	    header.set(a, attrs.get(a));
	}
	    
	columnDefs = header;

	checkColumnDefinitions();
	return this;
    }

    /**
     * Add a row to the table.
     */
    public Table addRow(TableRow row) throws TableException, TypeException {
	if (row.size() != columnDefs.size()) {
	    // the row is the wrong size
	    throw new TableException("Row: " + getRowCount() + " is not the correct size " + row.size() + ".  Expected size is " + getColumnCount()); 
	} else {
	    // the row is the right size
	    // now check all the types
	    ProbeAttributeType columnType;
	    ProbeAttributeType valueType;

	    for (int v=0; v < row.size(); v++) {
		// check row has valid entries, by seeing if the
		// type of the value is the same as the type of the column
		TableValue value = row.get(v);
		columnType = columnDefs.get(v).getType();
		valueType = value.getType();

		if (valueType.equals(columnType)) {
		    // great, now move on
		} else {
		    throw new TypeException("Row: " + getRowCount() + 
					    " position: " + v +
					    " cannot be of type " + valueType +
					    " when column type is " + columnType);
		}
	    }

	    // if we get here everythin is OK
	    rows.add(row);
	    return this;
	}
    }

    /**
     * Add a row to the table.
     * Pass in a lit of TableValue.
     */
    public Table addRow(List<TableValue> values)  throws TableException, TypeException {
	if (values.size() != columnDefs.size()) {
	    // the list is the wrong size
	    throw new TableException("Row: " + getRowCount() + " is not the correct size " + values.size() + ".  Expected size is " + getColumnCount()); 
	} else {
	    // the list is the right size, so create a row from the list
	    TableRow row = new DefaultTableRow();

	    ProbeAttributeType columnType;
	    ProbeAttributeType valueType;

	    for (int v=0; v < values.size(); v++) {
		// check row has valid entries, by seeing if the
		// type of the value is the same as the type of the column
		TableValue value = values.get(v);
		columnType = columnDefs.get(v).getType();
		valueType = value.getType();
		if (valueType.equals(columnType)) {
		    row.set(v, value);
		} else {
		    throw new TypeException("Row: " + getRowCount() + 
					    " position: " + v +
					    " cannot be of type " + valueType +
					    " when column type is " + columnType);
		}
	    }
	    
	    rows.add(row);

	    return this;
	}
    }

    /**
     * Get a row.
     */
    public TableRow getRow(int rowNo) {
	return rows.get(rowNo);
    }

    /**
     * Get the number of columns in the table.
     */
    public int getColumnCount() {
	// check to see if all the definitions are correct.
	if (checkColumnDefinitions()) {
	    return columnDefs.size();
	} else {
	    throw new RuntimeException("Table Column Definitions are not complete");
	}
    }

    /**
     * Get the number of rows in the table.
     */
    public int getRowCount() {
	return rows.size();
    }


    /**
     * Get the list of all the column definitions.
     */
    public TableHeader getColumnDefinitions() {
	return columnDefs;
    }

    /**
     * Check column definitions.
     * That is all columns have a value, i.e. no nulls.
     */
    private boolean checkColumnDefinitions() {
	int colCount = columnDefs.size();
	for (int c=0; c<colCount; c++) {
	    if (columnDefs.get(c) == null) {
		return false;
	    }
	}

	// all the columns are defined OK
	return true;
    }

    /**
     * Convert the Table to a List of TableRows.
     */
    public List<TableRow> toList() {
	return rows;
    }

    /**
     * To String for a table, prints the header and the rows.
     */
    public String toString() {
	ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); 
	PrintStream out = new PrintStream(byteStream);

	out.println(getColumnDefinitions());
	for (int r=0; r<getRowCount(); r++) {
	    out.println(getRow(r));
	}

	return byteStream.toString();
    }
}