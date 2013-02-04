// TableSerialize1.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Feb 2009

package eu.reservoir.demo;

import eu.reservoir.monitoring.core.table.*;
import eu.reservoir.monitoring.core.ProbeAttributeType;
import eu.reservoir.monitoring.core.TypeException;
import java.util.*;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A test of the Table type.
 */
public class TableSerialize1 {
    public static void main(String[] args) throws TableException, TypeException {
	// allocate a table
	Table table1 = new DefaultTable();

	// define the header
	TableHeader header = new DefaultTableHeader().
	    add("name", ProbeAttributeType.STRING).
	    add("type", ProbeAttributeType.STRING);

	table1.defineTable(header);

	// add a row of values
	TableRow r0 = new DefaultTableRow().
	    add(new DefaultTableValue("stuart")).
	    add(new DefaultTableValue("person"));

	table1.addRow(r0);

	table1.addRow(new DefaultTableRow().add("hello").add("world"));


	System.out.println(table1.getColumnDefinitions());
	System.out.println(table1.getRowCount());

	System.out.println(table1);
	System.out.println("serialize: ");
	byte[] bytes = construct(table1);
	System.out.printf("%d\n", bytes.length);
	System.out.println(": " + new String(bytes));
    }

    private static byte[] construct(Serializable object) {
	// Create a ByteArrayOutputStream to store the serialized 
	// version of the objects
	ByteArrayOutputStream bos = new ByteArrayOutputStream();

	try {
	    // Wrap the ByteArrayOutputStream in an ObjectOutputStream
	    ObjectOutputStream oos = new ObjectOutputStream(bos);
	    // and write the objects to the stream
	    oos.writeObject(object);
	    oos.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new Error("Failed to write serializable data for " + object);
	}

	// the bytes are now held in the ByteArrayOutputStream
	// so we get the bytes of the ByteArrayOutputStream
	byte[] rawBytes = bos.toByteArray();

	return rawBytes;

    }

}